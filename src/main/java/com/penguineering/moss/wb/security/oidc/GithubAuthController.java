package com.penguineering.moss.wb.security.oidc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penguineering.moss.wb.security.directory.MossUserDTO;
import com.penguineering.moss.wb.security.directory.MossUserRepository;
import com.penguineering.moss.wb.security.mapping.OidcMapping;
import com.penguineering.moss.wb.security.mapping.OidcMappingKey;
import com.penguineering.moss.wb.security.mapping.OidcMappingRepository;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.RetrySpec;

import java.net.URI;
import java.util.*;

@ConditionalOnProperty(value = {
        "auth.clients.github.client-id",
        "auth.clients.github.client-secret"})
@PermitAll
@RestController
@RequestMapping("/auth/github")
public class GithubAuthController {
    private final String clientId;
    private final String clientSecret;
    private final String callbackBaseUrl;
    private final String webappBaseUrl;
    private final Map<String, String> currentStates = new HashMap<>();

    private final OidcMappingRepository oidcMappingRepository;
    private final MossUserRepository mossUserRepository;

    public GithubAuthController(@Value("${webapp-base-url}") String webappBaseUrl,
                                OidcClientConfiguration oidcClientConfig,
                                MossUserRepository mossUserRepository,
                                OidcMappingRepository oidcMappingRepository) {
        if (!oidcClientConfig.getClients().containsKey("github"))
            throw new IllegalArgumentException("No github client configured");

        this.clientId = oidcClientConfig.getClients().get("github").getClientId();
        this.clientSecret = oidcClientConfig.getClients().get("github").getClientSecret();
        this.callbackBaseUrl = oidcClientConfig.getCallbackBaseUrl();
        this.webappBaseUrl = webappBaseUrl;
        this.mossUserRepository = mossUserRepository;
        this.oidcMappingRepository = oidcMappingRepository;
    }

    @GetMapping
    public Mono<ResponseEntity<Void>> authenticate(@Nullable String redirect, WebSession session) {
        return Mono
                // invalidate session
                .just(session)
                .map(WebSession::invalidate)
                // then generate state
                .then(Mono.just(UUID.randomUUID().toString()))
                .doOnNext(state -> {
                    synchronized (currentStates) {
                        // store state and it's redirect target
                        currentStates.put(state, Objects.requireNonNullElse(redirect, ""));
                    }
                })
                // render callback URL
                .map(this::renderCallbackUrl)
                .map(uri -> ResponseEntity
                        .status(HttpStatus.SEE_OTHER)
                        .location(uri)
                        .build());
    }

    private URI renderCallbackUrl(String state) {
        return URI.create(
                String.format(
                        "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&state=%s",
                        clientId,
                        URI.create(callbackBaseUrl).resolve("auth/github/callback"),
                        state));
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<Map<String, String>>> callback(@RequestParam @Nullable String code,
                                                              @RequestParam String state,
                                                              @RequestParam @Nullable String error,
                                                              @RequestParam @Nullable String error_description,
                                                              @RequestParam @Nullable String error_uri,
                                                              WebSession session) {
        // check state and retrieve redirect target, if available
        final String redirect;
        synchronized (currentStates) {
            if (!currentStates.containsKey(state))
                return Mono.just(ResponseEntity.badRequest().body(
                        Map.of("error", "Invalid state")));
            redirect = Objects.requireNonNullElse(currentStates.remove(state), "");
        }

        // handle a potential error
        if (error != null) {
            Map<String, String> body = new HashMap<>();
            body.put("error", error);
            if (Objects.nonNull(error_description))
                body.put("error_description", error_description);
            if (Objects.nonNull(error_uri))
                body.put("error_uri", error_uri);

            return Mono.just(
                    ResponseEntity
                            .badRequest()
                            .body(body));
        }

        // Code is null in case of an error, but must not be null otherwise
        if (Objects.isNull(code))
            return Mono.just(
                    ResponseEntity
                            .badRequest()
                            .body(Map.of("error", "No code provided")));

        return Mono.just(Tuples.of(state, code))
                // Get OIDC specific information
                .zipWhen(this::retrieveFromOidcProvider,
                        (t, res) -> res)
                .flatMap(oidcInfo -> handleLoginCallback(oidcInfo, session, redirect));
    }


    protected Mono<ResponseEntity<Map<String, String>>> handleLoginCallback(
            Tuple2<OidcMappingKey, MossUserDTO> oidcInfo,
            WebSession session,
            String redirect) {
        return Mono.just(oidcInfo)
                .doOnNext(t -> t.getT1().saveToWebSession(session))
                .doOnNext(t -> t.getT2().saveToWebSession(session))
                // Try to find mapping
                .zipWhen(t -> Mono.just(t.getT1())
                                .publishOn(Schedulers.boundedElastic())
                                .map(oidcMappingRepository::findById)
                                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty)),
                        (t, id) -> id)
                // Try to find user, note that if there is no mapping, the Mono is empty here!
                .zipWhen(mapping -> Mono.just(mapping)
                                .publishOn(Schedulers.boundedElastic())
                                .map(OidcMapping::getMossUserId)
                                .map(mossUserRepository::findById)
                                .flatMap(o -> o.map(Mono::just).orElseGet(Mono::empty)),
                        (t, user) -> user)
                // if there is a user, store to session (overwrite previous DTO, which did not have a UUID yet)
                .doOnNext(user -> user.toMossUserDTO().saveToWebSession(session))
                // in this case we can redirect
                .map(user -> URI.create(webappBaseUrl).resolve(redirect))
                // otherwise redirect to login page, the user is still stored in  the session
                .switchIfEmpty(Mono.just(URI.create(webappBaseUrl).resolve("login")))
                // send a forward to that target
                .map(l -> ResponseEntity
                        .status(HttpStatus.SEE_OTHER)
                        .location(l)
                        .build());
    }

    private Mono<Tuple2<OidcMappingKey, MossUserDTO>> retrieveFromOidcProvider(Tuple2<String, String> tStateCode) {
        return retrieveAccessToken(tStateCode.getT1(), tStateCode.getT2())
                .flatMap(this::retrieveUserInfo);
    }

    private Mono<String> retrieveAccessToken(String state, String code) {
        return Optional.ofNullable(code)
                .map(c -> WebClient.create("https://github.com/login/oauth/access_token")
                        .post()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(Map.of(
                                "client_id", clientId,
                                "client_secret", clientSecret,
                                "code", c,
                                "state", state
                        )))
                        .exchangeToMono(this::extractAccessToken)
                        .retryWhen(RetrySpec
                                .backoff(3, java.time.Duration.ofMillis(500))
                                .transientErrors(true)))
                .orElse(Mono.empty());
    }

    private Mono<String> extractAccessToken(ClientResponse response) {
        if (response.statusCode().isError())
            return Mono.error(new RuntimeException("Error while retrieving access token: " + response.statusCode()));
        return response
                .bodyToMono(Map.class)
                .map(map -> (String) map.get("access_token"));
    }

    private Mono<Tuple2<OidcMappingKey, MossUserDTO>> retrieveUserInfo(String accessToken) {
        return WebClient.create("https://api.github.com/user")
                .get()
                .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                .header(HttpHeaders.USER_AGENT, "MOSS Backend Agent")
                .exchangeToMono(this::extractUserInfo)
                .retryWhen(RetrySpec
                        .backoff(3, java.time.Duration.ofMillis(500))
                        .transientErrors(true));
    }

    private Mono<Tuple2<OidcMappingKey, MossUserDTO>> extractUserInfo(ClientResponse clientResponse) {
        if (clientResponse.statusCode().isError())
            return Mono.error(new RuntimeException("Error while retrieving user info: " + clientResponse.statusCode()));
        return clientResponse
                .bodyToMono(Map.class)
                .map(GithubUserInfo::fromMap)
                .map(i -> Tuples.of(
                        i.toOidcMappingKey(),
                        i.toMossUserDTO()));
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    protected record GithubUserInfo(
            @JsonProperty("id") int id,
            @JsonProperty("login") String login,
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("avatar_url") String avatarUrl) {

        public static GithubUserInfo fromMap(Map<String, Object> map) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(map, GithubUserInfo.class);
        }

        public MossUserDTO toMossUserDTO() {
            return new MossUserDTO(null, name, email, URI.create(avatarUrl));
        }

        public OidcMappingKey toOidcMappingKey() {
            return new OidcMappingKey("github", String.valueOf(id));
        }

        @Override
        public String toString() {
            return login + "(" + id + ")";
        }
    }
}