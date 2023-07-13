package com.ospx.sock;

import arc.func.Cons;
import arc.struct.*;
import arc.util.*;
import lombok.Getter;

@Getter
@SuppressWarnings("unchecked")
public class EventBus {
    private final ObjectMap<Class<?>, Seq<Subscription<?>>> events = new ObjectMap<>();

    public EventBus() {
        Timer.schedule(this::clearExpired, .1f, .1f);
    }

    public <T> Subscription<T> on(T value, Runnable listener) {
        return on((Class<T>) value.getClass(), event -> {
            if (event.equals(value))
                listener.run();
        });
    }

    public <T> Subscription<T> on(Class<T> type, Cons<T> listener) {
        var listeners = events.get(type, Seq::new);

        var subscription = new Subscription<>(listeners, listener);
        listeners.add(subscription);

        return subscription;
    }

    public <T> void fire(T value) {
        var listeners = events.get(value.getClass());
        if (listeners == null) return;

        for (var subscription : listeners)
            ((Cons<T>) subscription.getListener()).get(value);
    }

    public boolean contains(Class<?> type) {
        return events.containsKey(type);
    }

    public void clear() {
        events.clear();
    }

    public void clearExpired() {
        for (var subscriptions : events.values()) {
            subscriptions.each(Subscription::expired, subscription -> {
                if (subscription.unsubscribe()) {
                    var expired = subscription.getExpired();
                    if (expired == null) return;

                    expired.run();
                }
            });
        }
    }

    @Getter
    public static class Subscription<T>  {
        private final Seq<Subscription<?>> listeners;
        private final Cons<T> listener;

        private long expireAt = -1L;
        private Runnable expired;

        Subscription(Seq<Subscription<?>> listeners, Cons<T> listener) {
            this.listeners = listeners;
            this.listener = listener;
        }

        public Subscription<T> withTimeout(long timeout) {
            this.expireAt = Time.millis() + timeout;
            return this;
        }

        public Subscription<T> withTimeout(long timeout, Runnable expired) {
            this.expireAt = Time.millis() + timeout;
            this.expired = expired;
            return this;
        }

        public boolean expired() {
            return expireAt >= 0 && Time.millis() >= expireAt;
        }

        public boolean unsubscribe() {
            return listeners.remove(this);
        }
    }
}