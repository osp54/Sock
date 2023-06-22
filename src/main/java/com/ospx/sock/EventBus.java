package com.ospx.sock;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Timer;

public class EventBus {
    private final ObjectMap<Object, Seq<Subscription<? extends Object>>> events = new ObjectMap<>();

    public EventBus() {
        Timer.schedule(this::clearExpired, 0.100f, 0.100f);
    }

    public <T> Subscription<T> on(T type, Runnable listener) {
        Seq<Subscription<?>> listeners = events.get(type);
        if (listeners == null) {
            listeners = new Seq<>();
            events.put(type, listeners);
        }

        Cons<?> cons = event -> listener.run();
        Subscription<?> subscription = new Subscription<>(listeners, cons);
        listeners.add(subscription);
        return (Subscription<T>) subscription;
    }

    public <T> Subscription<T> on(Class<T> type, Cons<T> listener) {
        Seq<Subscription<?>> listeners = events.get(type);
        if (listeners == null) {
            listeners = new Seq<>();
            events.put(type, listeners);
        }

        Subscription<?> subscription = new Subscription<>(listeners, listener);
        listeners.add(subscription);
        return (Subscription<T>) subscription;
    }

    public <T> boolean unsubscribe(Subscription<T> subscription) {
        Seq<Subscription<?>> listeners = subscription.getListeners();
        Subscription<?> listener = (Subscription<?>) subscription.getListener();
        return listeners != null && listener != null && listeners.remove(listener);
    }

    public boolean contains(Object type) {
        return events.containsKey(type);
    }

    public <T> void fire(T value) {
        fire(value, value);
        fire(value.getClass(), value);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> void fire(Object type, T value) {
        Seq<Subscription<?>> listeners = events.get(type);
        if (listeners == null)
            return;

        for (Subscription<?> subscription : listeners) {
            try {
                ((Cons<T>) subscription.getListener()).get(value);
            } catch (Exception e) {
                Log.err(e);
            }
        }
    }

    public void clear() {
        events.clear();
    }

    public void clearExpired() {
        for (var subcribers : events.values()) {
            for (var subscriber : subcribers) {
                if (subscriber.expired()) {
                    subscriber.onExpire.run();
                    subscriber.unsubscribe();
                }
            }
        }
    }

    public static class Subscription<T> {
        private final Seq<Subscription<? extends Object>> listeners;
        private final Cons<T> listener;

        private long expireAt = -1L;
        private Runnable onExpire;

        Subscription(Seq<Subscription<?>> listeners, Cons<T> listener) {
            this.listeners = listeners;
            this.listener = listener;
        }

        Seq<Subscription<?>> getListeners() {
            return listeners;
        }

        Cons<T> getListener() {
            return listener;
        }

        public Subscription<T> expireAfter(long ms) {
            return expireAfter(ms, () -> {
            });
        }

        public Subscription<T> expireAfter(long ms, Runnable onExpire) {
            this.expireAt = Time.millis() + ms;
            this.onExpire = onExpire;
            return this;
        }

        public boolean expired() {
            return expireAt != -1 && Time.millis() >= expireAt;
        }

        public void unsubscribe() {
            if (listeners != null && listener != null) {
                listeners.remove(this);
            }
        }
    }
}
