package com.rewedigital.composer.application;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.rewedigital.composer.composing.ResponseCompositionHandler;
import com.rewedigital.composer.routing.BackendRouting;
import com.rewedigital.composer.routing.RouteTypes;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;

import okio.ByteString;

/**
 * Central request handler that handles all requests by delegating them to the
 * appropriate route type.
 */
public class RequestHandler {

    private final BackendRouting routing;
    private final RouteTypes routeTypes;
    private final ResponseCompositionHandler compositionHandler;

    public RequestHandler(final BackendRouting routing, final RouteTypes routeTypes,
            final ResponseCompositionHandler compositionHandler) {
        this.routing = Objects.requireNonNull(routing);
        this.routeTypes = Objects.requireNonNull(routeTypes);
        this.compositionHandler = Objects.requireNonNull(compositionHandler);

    }

    public CompletionStage<Response<ByteString>> execute(final RequestContext context) {
        return compositionHandler.initializeFrom(context).thenCompose(composition -> {
            return routing.matches(context.request())
                    .map(rm -> rm.routeType(routeTypes)
                            .execute(rm, context, composition))
                    .orElse(defaultResponse());
        });
    }

    private static CompletableFuture<Response<ByteString>> defaultResponse() {
        final Response<ByteString> response = Response.of(Status.INTERNAL_SERVER_ERROR,
                ByteString.encodeUtf8("Ohh.. noose!"));
        return CompletableFuture.completedFuture(response);
    }
}
