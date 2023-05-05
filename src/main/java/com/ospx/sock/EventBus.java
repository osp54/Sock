package com.ospx.sock;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;

/**
 * Copied from {@link arc.Events}
 */
public class EventBus {
    public final ObjectMap<Object, Seq<Cons<?>>> events = new ObjectMap<>();

    /**
     * Handle an event by class.
     */
    public <T> void on(Class<T> type, Cons<T> listener) {
        events.get(type, Seq::new).add(listener);
    }

    /**
     * Handle an event by enum.
     */
    public void run(Object type, Runnable listener) {
        events.get(type, Seq::new).add(event -> listener.run());
    }

    /**
     * Remove an event by index.
     */
    public <T> Cons<?> remove(Class<T> type, int index) {
        return events.get(type, Seq::new).remove(index);
    }

    /**
     * Remove an event by reference.
     */
    public <T> boolean remove(Class<T> type, Cons<T> listener) {
        return events.get(type, Seq::new).remove(listener);
    }

    /**
     * Fires an event by enum.
     */
    public <T extends Enum<T>> void fire(Enum<T> value) {
        fire(value, value);
    }

    /**
     * Fires an event by value.
     */
    public <T> void fire(T value) {
        fire(value.getClass(), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void fire(Object type, T value) {
        Seq<Cons<?>> listeners = events.get(type);
        if (listeners == null) return;

        for (Cons cons : listeners)
            cons.get(value);
    }

    /**
     * Clears all event listeners. Don't do this.
     */
    public void clear() {
        events.clear();
    }
}