package com.ospx.sock;

import arc.func.Cons;
import arc.util.Log;

public abstract class Sock {

    public static Sock client(int port) {
        return new ClientSock(port);
    }

    public static Sock client(String ip, int port) {
        return new ClientSock(ip, port);
    }

    public static Sock server(int port) {
        return new ServerSock(port);
    }

    public final EventBus bus = new EventBus();

    public abstract void connect();
    public abstract void disconnect();

    public abstract void sendEvent(Object object);

    public <T> void onEvent(T type, Runnable runnable) {
        bus.on(type, runnable);
    }

    public <T> void onEvent(Class<T> type, Cons<T> cons) {
        bus.on(type, cons);
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

    public static void main(String[] args) {
        Sock client = Sock.client(2000);
    }
}