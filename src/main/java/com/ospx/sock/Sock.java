package com.ospx.sock;

import arc.net.*;
import arc.util.Log;

import java.io.IOException;

public class Sock {
    public EndPoint socket;
    public Mode mode;

    public static Sock client(String ip, int port) throws IOException {
        return new Sock(ip, port, Mode.CLIENT);
    }

    public static Sock server(int port) throws IOException {
        return new Sock(null, port, Mode.SERVER);
    }

    public Sock(String ip, int port, Mode mode) throws IOException {
        this.mode = mode;
        switch (mode) {
            case CLIENT -> {
                socket = new Client(32768, 16384, new PacketSerializer());
                socket.addListener(new SockNetListener(socket));

                socket.start();
                ((Client) socket).connect(3000, ip, port);
            }
            case SERVER -> {
                socket = new Server(32768, 16384, new PacketSerializer());
                socket.addListener(new SockNetListener(socket));

                ((Server) socket).bind(port);
                socket.start();
            }
        }
    }

    public <T> void sendEvent(T event) {

    }

    public static void main(String[] args) throws IOException {
        SockEvents.on(TestEvent.class, e -> {
            Log.info(e.name);
        });

        Sock server = Sock.server(2000);
        Sock client = Sock.client("127.0.01", 2000);

        ((Client) client.socket).sendTCP(new TestEvent("ospx"));
    }

    public enum Mode {
        SERVER, CLIENT
    }
}