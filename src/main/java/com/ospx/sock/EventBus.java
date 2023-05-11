package com.ospx.sock;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;

/**
 * Copied from {@link arc.Events}
 */
public class EventBus {
    public final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    public <T> void on(T type, Runnable listener) {
        events.get(type, Seq::new).add(event -> listener.run());
    }

    public <T> void on(Class<T> type, Cons<T> listener) {
        events.get(type, Seq::new).add(listener);
    }

    public <T> Cons<?> remove(Class<T> type, int index) {
        return events.get(type, Seq::new).remove(index);
    }

    public <T> boolean remove(Class<T> type, Cons<T> listener) {
        return events.get(type, Seq::new).remove(listener);
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
        var listeners = events.get(type);
        if (listeners == null) return;

        for (Cons cons : listeners)
            try {
                cons.get(value);
            } catch (Exception e) {
                Log.err(e);
            }
    }

    public void clear() {
        events.clear();
    }
}