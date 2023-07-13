package com.ospx.sock;

import arc.func.Cons;
import arc.mock.MockApplication;
import arc.util.*;
import com.ospx.sock.EventBus.Subscription;
import lombok.Getter;

import static arc.Core.*;

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

    public static void main(String[] args) {
        app = new MockApplication();

        var server = Sock.server(2000);
        var client = Sock.client(2000);

        server.connect();
        client.connect();

        server.on("nya", () -> Log.info("ne nya")).withTimeout(5, () -> {
            Log.info("timeout");
        });

        Timer.schedule(() -> {
            client.send("nya");
        }, 0, 1, 30);

        while (true) {
        }
    }

    public abstract void connect();

    public abstract void disconnect();

    public abstract void send(Object object);

    public <T> Subscription<T> on(T type, Runnable runnable) {
        return bus.on(type, runnable);
    }

    public <T> Subscription<T> on(Class<T> type, Cons<T> cons) {
        return bus.on(type, cons);
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