# grpc-samples

An end to end example with a `grpc-server` that produces a message, a `grpc-client` that consumes it and a `grpc-gateway` that routes the requests.

First we need to start the server that is going to be listing to requests:

```shell
 ./gradlew :grpc-server:bootRun
```
Then, we start the gateway that is going to re-route the gRPC requests:
```shell
./gradlew :grpc-gateway:bootRun
```

Finally, we can use the client that points to the gateway application:
```shell
./gradlew :grpc-client:bootRun
```

The gateway routes and filters can be modified in `grpc-gateway/src/main/resources/application.yaml`

At the moment there is just one route that will forward everything to the `grpc-server`:
```yaml
      routes:
        - id: grpc
          uri: https://localhost:6565
          predicates:
            - Path=/**
          filters:
            - AddResponseHeader=X-Request-red, blue
```