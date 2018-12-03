package com.rewedigital.composer.session;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.rewedigital.composer.composing.ComposableRoot;
import com.rewedigital.composer.composing.CompositionStep;
import com.rewedigital.composer.composing.RequestEnricher;
import com.spotify.apollo.Request;
import com.spotify.apollo.Response;

/**
 * Describes the <em>root</em> session of a request. The root session is
 * constructed from a data map, can be written as a set of http headers to a
 * request and can be updated ({@link #composedWith(SessionFragment)}) with data
 * from a {@link SessionFragment}. A root session is dirty if after a merge the
 * data has changed.
 *
 * A root session can be written to a response using an instance of a
 * {@link SessionRoot.Serializer}.
 *
 */
public class SessionRoot implements ComposableRoot<SessionFragment>, RequestEnricher {

    public interface Serializer {
        <T> Response<T> writeTo(final Response<T> response, final Map<String, String> sessionData, boolean dirty);
    }

    private static final Serializer noopSerializer = new Serializer() {
        @Override
        public <T> Response<T> writeTo(final Response<T> response, final Map<String, String> sessionData,
                final boolean dirty) {
            return response;
        }
    };

    private static final String sessionIdKey = "session-id";
    private static final SessionRoot emptySession = new SessionRoot(noopSerializer, new HashMap<>(), false);

    private final SessionData data;
    private final boolean dirty;
    private final Serializer serializer;

    private SessionRoot(final Serializer serializer, final Map<String, String> data, final boolean dirty) {
        this(serializer, new SessionData(data), dirty);
    }

    private SessionRoot(final Serializer serializer, final SessionData data, final boolean dirty) {
        this.serializer = Objects.requireNonNull(serializer);
        this.data = Objects.requireNonNull(data);
        this.dirty = dirty;
    }

    public static SessionRoot empty() {
        return emptySession;
    }

    public static SessionRoot of(final Serializer serializer, final Map<String, String> data) {
        return of(serializer, data, false);
    }

    public static SessionRoot of(final Serializer serializer, final Map<String, String> data, final boolean dirty) {
        return new SessionRoot(serializer, data, dirty);
    }

    @Override
    public Request enrich(final Request request) {
        return request.withHeaders(asHeaders());
    }

    public Optional<String> get(final String key) {
        return data.get(key);
    }

    public Optional<String> getId() {
        return get(sessionIdKey);
    }

    @Override
    public <T> Response<T> writtenTo(final Response<T> response) {
        return serializer.writeTo(response, asHeaders(), dirty);
    }

    @Override
    public SessionRoot composedWith(final SessionFragment other) {
        final SessionData mergedData = data.mergedWith(other.data);
        final SessionData newData = getId().map(id -> mergedData.with(sessionIdKey, id)).orElse(mergedData);
        final boolean newDirty = !data.equals(newData);
        return new SessionRoot(serializer, newData, newDirty || dirty);
    }

    public SessionRoot withId(final String sessionId) {
        return new SessionRoot(serializer, data.with(sessionIdKey, sessionId), true);
    }

    public boolean isDirty() {
        return dirty;
    }

    public Map<String, String> rawData() {
        return data.rawData();
    }

    public SessionData data() {
        return data;
    }

    @Override
    public Class<SessionFragment> composableType() {
        return SessionFragment.class;
    }

    @Override
    public SessionFragment composableFor(final Response<?> response, final CompositionStep step) {
        return SessionFragment.of(response);
    }

    public Serializer serializer() {
        return this.serializer;
    }

    private Map<String, String> asHeaders() {
        return data.asHeaders();
    }
}
