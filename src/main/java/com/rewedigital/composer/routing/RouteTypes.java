package com.rewedigital.composer.routing;

import java.util.Objects;

import com.rewedigital.composer.composing.TemplateComposer;

/**
 * Factory creating a {@link RouteType} instance to handle a specific route type.
 */
public class RouteTypes {

    private final TemplateComposer.Factory composerFactory;
    private final CompositionAwareRequestClient templateClient;

    public RouteTypes(final TemplateComposer.Factory composerFactory,
            final CompositionAwareRequestClient templateClient) {
        this.templateClient = Objects.requireNonNull(templateClient);
        this.composerFactory = Objects.requireNonNull(composerFactory);
    }

    public ProxyRoute proxy() {
        return new ProxyRoute(templateClient);
    }

    public TemplateRoute template() {
        return new TemplateRoute(templateClient, composerFactory);
    }

}
