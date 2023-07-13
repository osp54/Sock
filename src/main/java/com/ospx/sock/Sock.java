package com.ospx.sock;

import arc.func.Cons;
import com.ospx.sock.EventBus.*;
import lombok.Getter;

@Getter
public abstract class Sock {

    public final EventBus bus = new EventBus(this);
    public final PacketSerializer serializer = new PacketSerializer(this);

    public static ClientSock client(int port) {
        return new ClientSock(port);
    }

    public static ServerSock server(int port) {
        return new ServerSock(port);
    }

    public abstract void connect();
    public abstract void disconnect();

    public abstract void send(Object object);

    public <T> EventSubscription<T> run(T value, Runnable listener) {
        return bus.run(value, listener);
    }

    public <T> EventSubscription<T> on(Class<T> type, Cons<T> listener) {
        return bus.on(type, listener);
    }

    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener) {
        var subscription = bus.request(request, listener);
        send(request);

        return subscription;
    }

    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, float seconds) {
        var subscription = bus.request(request, listener).withTimeout(seconds);
        send(request);

        return subscription;
    }

    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, Runnable expired) {
        var subscription = bus.request(request, listener).withTimeout(expired);
        send(request);

        return subscription;
    }

    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, Runnable expired, float seconds) {
        var subscription = bus.request(request, listener).withTimeout(seconds, expired);
        send(request);

        return subscription;
    }

    public <T extends Response> void respond(Request<T> request, T response) {
        response.setUuid(request.getUuid());
        send(response);
    }

    public boolean isConnected() {
        return true;
    }

    public boolean isServer() {
        return this instanceof ServerSock;
    }

    public boolean isClient() {
        return this instanceof ClientSock;
    }
}