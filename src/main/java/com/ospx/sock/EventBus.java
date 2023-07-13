package com.ospx.sock;

import arc.func.Cons;
import arc.struct.*;
import arc.util.*;
import arc.util.Timer.Task;
import lombok.Getter;

@Getter
@SuppressWarnings("unchecked")
public class EventBus {
    private static int lastRequestID = 0;
    private final Sock sock;

    private final IntMap<RequestSubscription<?>> requests = new IntMap<>();
    private final ObjectMap<Class<?>, Seq<EventSubscription<?>>> events = new ObjectMap<>();

    public EventBus(Sock sock) {
        this.sock = sock;
    }

    // region methods

    public <T> EventSubscription<T> run(T value, Runnable listener) {
        return on((Class<T>) value.getClass(), event -> {
            if (event.equals(value))
                listener.run();
        });
    }

    public <T> EventSubscription<T> on(Class<T> type, Cons<T> listener) {
        var listeners = events.get(type, Seq::new);

        var subscription = new EventSubscription<>(type, listener);
        listeners.add(subscription);

        return subscription;
    }

    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener) {
        var subscription = new RequestSubscription<>(request.id, listener);
        requests.put(request.id, subscription);

        return subscription;
    }

    public <T> void fire(T value) {
        if (value instanceof Response response) {
            var subscription = requests.remove(response.id);
            if (subscription == null) return;

            ((Cons<T>) subscription.listener).get(value);
            return;
        }

        var listeners = events.get(value.getClass());
        if (listeners == null) return;

        for (var subscription : listeners)
            ((Cons<T>) subscription.listener).get(value);
    }

    public boolean contains(Class<?> type) {
        return events.containsKey(type);
    }

    public void clear() {
        events.clear();
    }

    // endregion
    // region classes

    @Getter
    public abstract static class Subscription<T> {
        protected @Nullable Task task;

        public Subscription<T> withTimeout(float seconds) {
            this.task = Timer.schedule(this::unsubscribe, seconds);
            return this;
        }

        public Subscription<T> withTimeout(float seconds, Runnable expired) {
            this.task = Timer.schedule(() -> {
                if (unsubscribe())
                    expired.run();
            }, seconds);

            return this;
        }

        public abstract boolean unsubscribe();
    }

    @Getter
    public class EventSubscription<T> extends Subscription<T> {
        protected final Class<T> type;
        protected final Cons<T> listener;

        EventSubscription(Class<T> type, Cons<T> listener) {
            this.type = type;
            this.listener = listener;
        }

        @Override
        public EventSubscription<T> withTimeout(float seconds) {
            super.withTimeout(seconds);
            return this;
        }

        @Override
        public EventSubscription<T> withTimeout(float seconds, Runnable expired) {
            super.withTimeout(seconds, expired);
            return this;
        }

        @Override
        public boolean unsubscribe() {
            if (task != null && task.isScheduled())
                task.cancel(); // Cancel the expiration task just in case

            return events.containsKey(type) && events.get(type).remove(this, true);
        }
    }

    @Getter
    public class RequestSubscription<T extends Response> extends Subscription<T> {
        private final int id;
        private final Cons<T> listener;

        RequestSubscription(int id, Cons<T> listener) {
            this.id = id;
            this.listener = listener;
        }

        @Override
        public RequestSubscription<T> withTimeout(float seconds) {
            super.withTimeout(seconds);
            return this;
        }

        @Override
        public RequestSubscription<T> withTimeout(float seconds, Runnable expired) {
            super.withTimeout(seconds, expired);
            return this;
        }

        @Override
        public boolean unsubscribe() {
            if (task != null && task.isScheduled())
                task.cancel(); // Cancel the expiration task just in case

            return requests.remove(id) != null;
        }
    }

    @Getter
    public abstract static class Request<T extends Response> {
        private final int id = lastRequestID++;
    }

    @Getter
    public abstract static class Response {
        private final int id;

        public Response(Request<?> request) {
            this.id = request.id;
        }
    }

    // endregion
}