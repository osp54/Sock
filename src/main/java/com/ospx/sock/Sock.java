package com.ospx.sock;

import arc.func.Cons;
import arc.util.Log;

public interface Sock {
    static Sock client(int port) {
        return new ClientSock(port);
    }

    static Sock server(int port) {
        return new ServerSock(port);
    }

    void connect();
    void disconnect();

    void sendEvent(Object object);
    <T> void onEvent(Class<T> type, Cons<T> consumer);

    static void main(String[] args) {
        var server = Sock.server(2000);
        var client = Sock.client(2000);

        server.connect();
        client.connect();

        server.onEvent(String.class, Log::info);
        //client.onEvent(String.class, Log::info);

        client.sendEvent("Hello world!");
    }
}