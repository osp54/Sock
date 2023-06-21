package com.ospx.sock;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;

public class EventBus {
    private final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    public <T> Subscription<T> on(T type, Runnable listener) {
        Seq<Cons<?>> listeners = events.get(type);
        if (listeners == null) {
            listeners = new Seq<>();
            events.put(type, listeners);
        }

        Cons<?> cons = event -> listener.run();
        listeners.add(cons);
        return new Subscription<>(listeners, cons);
    }

    public <T> Subscription<T> on(Class<T> type, Cons<T> listener) {
        Seq<Cons<?>> listeners = events.get(type);
        if (listeners == null) {
            listeners = new Seq<>();
            events.put(type, listeners);
        }

        listeners.add(listener);
        return new Subscription<>(listeners, listener);
    }

    public <T> boolean unsubscribe(Subscription<T> subscription) {
        Seq<Cons<?>> listeners = subscription.getListeners();
        Cons<?> listener = subscription.getListener();
        return listeners != null && listener != null && listeners.remove(listener);
    }

    public boolean contains(Object type) {
        return events.containsKey(type);
    }

    public <T> void fire(T value) {
        fire(value, value);
        fire(value.getClass(), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void fire(Object type, T value) {
        Seq<Cons<?>> listeners = events.get(type);
        if (listeners == null) return;

        for (Cons cons : listeners) {
            try {
                cons.get(value);
            } catch (Exception e) {
                Log.err(e);
            }
        }
    }

    public void clear() {
        events.clear();
    }

    public static class Subscription<T> {
        private final Seq<Cons<?>> listeners;
        private final Cons<?> listener;

        Subscription(Seq<Cons<?>> listeners, Cons<?> listener) {
            this.listeners = listeners;
            this.listener = listener;
        }

        Seq<Cons<?>> getListeners() {
            return listeners;
        }

        Cons<?> getListener() {
            return listener;
        }

        public void unsubscribe() {
            if (listeners != null && listener != null) {
                listeners.remove(listener);
            }
        }
    }
}
