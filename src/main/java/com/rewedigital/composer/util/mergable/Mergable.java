package com.rewedigital.composer.util.mergable;

/**
 * Describes a type of objects that can be merged with each other.
 *  
 * @param <T>
 */
public interface Mergable<T extends Mergable<T>> {

    public T mergedWith(final T other);

    @SuppressWarnings("unchecked")
    default T mergedFrom(final Mergables mergables) {
        return mergables.get(this.getClass()).map(r -> this.mergedWith((T) r)).orElse((T) this);
    }
}