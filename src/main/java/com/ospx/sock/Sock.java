package com.ospx.sock;

import arc.func.Cons;
import com.ospx.sock.EventBus.*;
import lombok.Getter;

@Getter
public abstract class Sock {

    private final EventBus bus = new EventBus(this);
    private final PacketSerializer serializer = new PacketSerializer(this);

    /** Creates a new instance of ClientSock
     * @param port - the port to which this ClientSock will connect
     * @return a new instance of ClientSock
     */
    public static ClientSock client(int port) {
        return new ClientSock(port);
    }

    /** Creates a new instance of ServerSock
     * @param port - the port on which this ServerSock will bind
     * @return a new instance of ServerSock
     */
    public static ServerSock server(int port) {
        return new ServerSock(port);
    }

    /**
     * Connects the current Sock
     */
    public abstract void connect();

    /**
     * Disconnects the current Sock
     */
    public abstract void disconnect();

    /**
     * Sends an object across the Sock network
     * @param value the object to be sent
     */
    public abstract void send(Object value);

    /**
     * Subscribes this Sock to a value listener
     * @param value a value to subscribe
     * @param listener a listener to be called when this value is received
     * @return a subscription which can be unsubscribed or time limited
     */
    public <T> EventSubscription<T> run(T value, Runnable listener) {
        return bus.run(value, listener);
    }

    /**
     * Subscribes this Sock to a class listener
     * @param type a class to subscribe
     * @param listener a listener to be called when an object of this class is received
     * @return a subscription which can be unsubscribed or time limited
     */
    public <T> EventSubscription<T> on(Class<T> type, Cons<T> listener) {
        return bus.on(type, listener);
    }

    /**
     * Creates a response subscription and sends a request across the Sock network
     * @param request a request to be sent
     * @param listener a listener to be called when any response to this request is received
     * @return a subscription which can be unsubscribed or time limited
     */
    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener) {
        var subscription = bus.request(request, listener);
        send(request);

        return subscription;
    }

    /**
     * Creates a response subscription and sends a request across the Sock network
     * @param request a request to be sent
     * @param listener a listener to be called when any response to this request is received
     * @param seconds the number of seconds after which this request will expire
     * @return a subscription which can be unsubscribed or time limited
     */
    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, float seconds) {
        var subscription = bus.request(request, listener).withTimeout(seconds);
        send(request);

        return subscription;
    }

    /**
     * Creates a response subscription and sends a request across the Sock network
     * @param request a request to be sent
     * @param listener a listener to be called when any response to this request is received
     * @param expired a listener to be called if no response is received for a certain time
     * @return a subscription which can be unsubscribed or time limited
     */
    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, Runnable expired) {
        var subscription = bus.request(request, listener).withTimeout(expired);
        send(request);

        return subscription;
    }

    /**
     * Creates a response subscription and sends a request across the Sock network
     * @param request a request to be sent
     * @param listener a listener to be called when any response to this request is received
     * @param seconds the number of seconds after which this request will expire
     * @param expired a listener to be called if no response is received for a certain time
     * @return a subscription which can be unsubscribed or time limited
     */
    public <T extends Response> RequestSubscription<T> request(Request<T> request, Cons<T> listener, Runnable expired, float seconds) {
        var subscription = bus.request(request, listener).withTimeout(seconds, expired);
        send(request);

        return subscription;
    }

    /**
     * Responds to a request across the Sock network
     * @param request a request to be responded
     * @param response a response to be sent
     */
    public <T extends Response> void respond(Request<T> request, T response) {
        response.setUuid(request.getUuid());
        send(response);
    }

    /**
     * @return whether this Sock instance is connected
     * @implNote the ServerSock is always connected, even if {@link Sock#connect()} wasn't called before
     */
    public boolean isConnected() {
        return true;
    }

    /**
     * @return whether this Sock instance is a ServerSock
     */
    public boolean isServer() {
        return this instanceof ServerSock;
    }

    /**
     * @return whether this Sock instance is a ClientSock
     */
    public boolean isClient() {
        return this instanceof ClientSock;
    }
}