# Customer Onboarding

A Spring Boot API for registering customers, logging in with the generated initial
password, and viewing an authenticated account overview.

## Run the application

### Prerequisites

- Docker with Docker Compose installed
- Port `8080` available on the host

From the project root, build and start the application with:

```shell
docker-compose up -d --build
```

The API is available at [http://localhost:8080](http://localhost:8080). The application uses an
embedded H2 database and applies its Flyway migrations automatically, so no separate database
setup is required.

To confirm that the container is running:

```shell
docker-compose ps
```

To inspect the application logs:

```shell
docker-compose logs -f app
```

To stop and remove the container:

```shell
docker-compose down
```

Because the database is in memory, application data is discarded when the container stops.

## Try the API with Postman

Import the [Customer Onboarding Postman collection](postman/customer-onboarding.postman_collection.json)
into Postman and run the collection in its defined order against the default `baseUrl` of
`http://localhost:8080`.

The collection covers:

- successful registration, login, and authenticated account overview;
- duplicate usernames and invalid registration details;
- incorrect credentials and missing or invalid bearer tokens; and
- concurrent requests that exercise the database rate limit.

The happy-path requests automatically carry the generated username, password, and access token
forward. Some requests include short delays so independent scenarios do not unintentionally exceed
the configured database limit of two operations per second.

## Run the automated tests

The Maven wrapper requires Java 21:

```shell
./mvnw test
```
