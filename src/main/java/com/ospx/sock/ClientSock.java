package com.ospx.sock;

import arc.net.Client;
import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.util.Log;
import lombok.SneakyThrows;

public class ClientSock extends Sock {

    public final Client client;
    public final String ip;
    public final int port;

    public ClientSock(int port) {
        this("localhost", port);
    }

    public ClientSock(String ip, int port) {
        this.client = new Client(32768, 16384, new PacketSerializer());
        this.ip = ip;
        this.port = port;

        this.client.addListener(new ClientSockListener());
    }

    @Override
    @SneakyThrows
    public void connect() {
        client.start();
        client.connect(5000, ip, port);
    }

    @Override
    @SneakyThrows
    public void disconnect() {
        client.close();
    }

    @Override
    public void sendEvent(Object object) {
        bus.fire(object);
        client.sendTCP(object);
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
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
            Log.debug(">>>"+object.getClass());
            bus.fire(object);
        }
    }
}