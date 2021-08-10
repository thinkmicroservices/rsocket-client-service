mvn clean spring-boot:build-image
docker run --rm -p 8080:8080 rsocket-client-service:0.0.1-SNAPSHOT

 