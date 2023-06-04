package com.ospx.sock;

import arc.func.Cons;

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

    public Thread thread;
    public final EventBus bus = new EventBus();
    protected final PacketSerializer packetSerializer = new PacketSerializer(bus);

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

    public PacketSerializer getPacketSerializer() {
        return packetSerializer;
    }
    public static void main(String[] args) {
        Sock server = Sock.server(2000);
        Sock client = Sock.client(2000);

        server.connect();
        client.connect();
    }
}