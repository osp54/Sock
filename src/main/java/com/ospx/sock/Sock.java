package com.ospx.sock;

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

    void send(Object object);

    public static void main(String[] args) {
        var server = Sock.server(2000);
        var client = Sock.client(2000);

        server.connect();
        client.connect();

        SockEvents.on(String.class, Log::info);
        client.send("Hello world!");
    }
}