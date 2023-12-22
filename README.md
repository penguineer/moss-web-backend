# MOSS Web UI Backend

> This is the Web UI Backend for [MOSS](https://github.com/penguineer/moss).



## Configuration

Configuration is done using environment variables:

* `PORT`: Port for the HTTP endpoint (default `8080`, only change when running locally!)
* `WEBUI_BASE_URI`: Base URI for the Web UI (defaults to `http://localhost:3000`)
* `OAUTH_CALLBACK_BASE_URI`: Base URI for the OAuth callback (defaults to `http://localhost:8080`)
* `GITHUB_OAUTH_CLIENT_ID`: GitHub OAuth Client ID (defaults to none and will disable GitHub authentication if not set)
* `GITHUB_OAUTH_CLIENT_SECRET`: GitHub OAuth Client Secret (defaults to none and will disable GitHub authentication if not set)
* `MYSQL_HOST`: MySQL host (defaults to `localhost`)
* `MYSQL_PORT`: MySQL port (defaults to `3306`)
* `MYSQL_DB`: MySQL database (defaults to `moss`)
* `MYSQL_USER`: MySQL user (defaults to `moss`)
* `MYSQL_PASS`: MySQL password

Please note that the base URI for the OAuth callback must match the URI configured in the GitHub OAuth application settings. 

## Development

This project uses the [Spring Boot](https://spring.io/projects/spring-boot).

Version numbers are determined with [jgitver](https://jgitver.github.io/).
Please check your [IDE settings](https://jgitver.github.io/#_ides_usage) to avoid problems, as there are still some unresolved issues.
If you encounter a project version `0` there is an issue with the jgitver generator.

For local execution the configuration can be provided in a `.env` file and made available using `dotenv`:
```bash
dotenv ./mvnw mn:run
```

Note that `.env` is part of the `.gitignore` and can be safely stored in the local working copy.


## Build

The build is split into two stages:
1. Packaging with [Maven](https://maven.apache.org/)
2. Building the Docker container

You can execute both stages with the following commands:

```bash
mvn --batch-mode --update-snapshots clean package
docker build .
```

It is important to note that the Dockerfile is designed to expect exactly one JAR file in the `target` directory.
Therefore, ensure that the Maven packaging process is completed successfully before proceeding to build the 
Docker container.

## Maintainers

* Stefan Haun ([@penguineer](https://github.com/penguineer))


## Contributing

PRs are welcome!

If possible, please stick to the following guidelines:

* Keep PRs reasonably small and their scope limited to a feature or module within the code.
* If a large change is planned, it is best to open a feature request issue first, then link subsequent PRs to this issue, so that the PRs move the code towards the intended feature.


## License

[MIT](LICENSE.txt) © 2023 Stefan Haun and contributors
