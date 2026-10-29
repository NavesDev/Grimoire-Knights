package com.navesdev.grimoireknights.core.network;

import java.util.function.Consumer;

/**
 * Lets common code register a client-bound payload without importing client classes:
 * the payload handler forwards here and the feature's client module sets the real handler.
 */
public final class ClientSink<T> {
    private volatile Consumer<T> handler = payload -> {};

    public void set(Consumer<T> handler) {
        this.handler = handler;
    }

    public void accept(T payload) {
        handler.accept(payload);
    }
}
