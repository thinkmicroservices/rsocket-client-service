package com.thinkmicroservices.rsocket.client.controller;

import com.thinkmicroservices.rsocket.client.message.Event;

import com.thinkmicroservices.rsocket.client.message.Request;
import com.thinkmicroservices.rsocket.client.message.Response;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author cwoodward
 */
@RestController
@Slf4j

public class RSocketClientController {

    private Environment env;

    private static final String SOURCE = "RSocket-Client" + UUID.randomUUID().toString();
    private static final String DESTINATION = "RSocket-Server";

    // RSocket Service routes
    public static final String RSOCKET_ROUTE_FIRE_AND_FORGET = "fire-and-forget";
    public static final String RSOCKET_ROUTE_REQUEST_RESPONSE = "request-response";
    public static final String RSOCKET_ROUTE_REQUEST_STREAM = "request-stream";
    public static final String RSOCKET_ROUTE_CHANNEL = "channel";

    // RSocket Client Service paths
    public static final String RSOCKET_PATH_PREFIX = "/";
    public static final String RSOCKET_FIRE_AND_FORGET_PATH = RSOCKET_PATH_PREFIX + RSOCKET_ROUTE_FIRE_AND_FORGET;
    public static final String RSOCKET_REQUEST_RESPONSE_PATH = RSOCKET_PATH_PREFIX + RSOCKET_ROUTE_REQUEST_RESPONSE;
    public static final String RSOCKET_REQUEST_STREAM_PATH = RSOCKET_PATH_PREFIX + RSOCKET_ROUTE_REQUEST_STREAM;
    public static final String RSOCKET_CHANNEL_PATH = RSOCKET_PATH_PREFIX + RSOCKET_ROUTE_CHANNEL;

    // static message content
    private static final String FIRE_AND_FORGET_CONTENT = "FIRE!!";
    private static final String REQUEST_RESPONSE_MESSAGE_CONTENT = "This is a request!";
    private final RSocketRequester rSocketRequester;

    public RSocketClientController(@Autowired RSocketRequester.Builder builder, @Autowired Environment env) {
        this.env = env;
        log.info("remote rsocket.server.host:" + env.getProperty("rsocket.server.host") + "=" + env.getProperty("rsocket.server.port"));
        this.rSocketRequester = builder.tcp(env.getProperty("rsocket.server.host"), Integer.parseInt(env.getProperty("rsocket.server.port")));
    }

    /**
     * Send a Fire-And-Forget request to the RSocketService.
     *
     * @return
     */
    @GetMapping(RSOCKET_FIRE_AND_FORGET_PATH)
    public Mono<String> fireAndForget() {
        Request request = Request.builder()
                .source(SOURCE)
                .destination(DESTINATION)
                .message(FIRE_AND_FORGET_CONTENT).build();
        log.info("Send RSocket request to 'fire-and-forget: " + request);
        Mono<Void> fnf = rSocketRequester
                .route(RSOCKET_ROUTE_FIRE_AND_FORGET)
                .data(request)
                .retrieveMono(Void.class);
        return fnf.thenReturn("Fire-and-Forget completed!");

    }

    
    /**
     * Send a Request/Response request to the RSocketService.
     *
     * @return
     */
    @GetMapping(RSOCKET_REQUEST_RESPONSE_PATH)
    public Mono<Response> requestResponse() {

        Request request = Request.builder()
                .source(SOURCE)
                .destination(DESTINATION)
                .message(REQUEST_RESPONSE_MESSAGE_CONTENT).build();
        log.info("Send  RSocket request to 'request-response: " + request);
        return rSocketRequester
                .route(RSOCKET_ROUTE_REQUEST_RESPONSE)
                .data(request)
                .retrieveMono(Response.class);
    }

   
    /**
     * Send a Request/Stream to the RSocketService.
     *
     * @return
     */
     @GetMapping(RSOCKET_REQUEST_STREAM_PATH)
    public ResponseEntity<Flux<Event>> requestStream() {

        Request request = Request.builder()
                .source(SOURCE)
                .destination(DESTINATION)
                .message("Get Events")
                .build();
        log.info("Send request to 'request-stream: " + request);
        Flux<Event> notificationFlux = rSocketRequester
                .route(RSOCKET_ROUTE_REQUEST_STREAM)
                .data(request)
                .retrieveFlux(Event.class);
        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(notificationFlux);
    }

    
    /**
     * Send a Channel request to the RSocketService.
     *
     * @return
     */
    @GetMapping(RSOCKET_CHANNEL_PATH)
    public ResponseEntity<Flux<Event>> channel() {

        Flux<Request> request1 = Flux.just(
                Request.builder()
                        .source(SOURCE)
                        .destination(DESTINATION)
                        .message("Request #1").build()
        );

        Flux<Request> requests = Flux.empty();
        requests = Flux.concat(requests, request1).delayElements(Duration.ofSeconds(2));

        //Flux.concat(requests, request1, request2, request3);
        log.info("send request flux to channel");
        Flux<Event> incomingEvents = this.rSocketRequester
                .route(RSOCKET_ROUTE_CHANNEL)
                .data(requests)
                .retrieveFlux(Event.class);
        log.info("retrieved inbound flux<event>");

        return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(incomingEvents);

    }

}
