package com.ospx.sock;

import arc.func.Cons;
import arc.struct.*;
import arc.util.*;
import lombok.Getter;

@Getter
@SuppressWarnings("unchecked")
public class EventBus {
    private final ObjectMap<Class<?>, Seq<Subscription<?>>> events = new ObjectMap<>();
    private final Sock sock;

    public EventBus(Sock sock) {
        this.sock = sock;
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

    @Getter
    public static class Subscription<T>  {
        private final Seq<Subscription<?>> listeners;
        private final Cons<T> listener;

        Subscription(Seq<Subscription<?>> listeners, Cons<T> listener) {
            this.listeners = listeners;
            this.listener = listener;
        }

        public void withTimeout(float seconds) {
            Timer.schedule(this::unsubscribe, seconds);
        }

        public void withTimeout(float seconds, Runnable expired) {
            Timer.schedule(() -> {
                if (unsubscribe())
                    expired.run();
            }, seconds);
        }

        public boolean unsubscribe() {
            return listeners.remove(this);
        }
    }
}