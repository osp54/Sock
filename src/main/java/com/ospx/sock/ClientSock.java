package com.ospx.sock;

import arc.net.Client;
import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.util.Log;
import lombok.SneakyThrows;

public class ClientSock implements Sock {

    public final Client client;
    public final int port;

    public ClientSock(int port) {
        this.client = new Client(32768, 16384, new PacketSerializer());
        this.client.addListener(new ClientSockListener());

        this.port = port;
    }

    @Override
    @SneakyThrows
    public void connect() {
        client.start();
        client.connect(3000, "localhost", port);
    }

    @Override
    @SneakyThrows
    public void disconnect() {
        client.close();
    }

    @Override
    public void send(Object object) {
        client.sendTCP(object);
    }

    public class ClientSockListener implements NetListener {

        @Override
        public void connected(Connection connection) {
            Log.info("[Sock] Connected to Sock server @. (@)", connection.getID(), connection.getRemoteAddressTCP());
        }

        @Override
        public void disconnected(Connection connection, DcReason reason) {
            Log.info("[Sock] Disconnected from Sock server @ due to @", connection.getID(), reason);
        }

        @Override
        public void received(Connection connection, Object object) {
            SockEvents.fire(object);
        }
    }
}