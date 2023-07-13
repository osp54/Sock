package com.ospx.sock;

import arc.net.*;
import arc.util.*;
import lombok.*;

import java.nio.channels.ClosedSelectorException;

@Getter
public class ClientSock extends Sock {
    private final Client client;
    private final int port;

    public ClientSock(int port) {
        this.client = new Client(32768, 16384, serializer);
        this.port = port;

        this.client.addListener(new MainThreadListener(new ClientSockListener()));
    }

    @Override
    @SneakyThrows
    public void connect() {
        Threads.daemon("Sock Client", () -> {
            try {
                client.run();
            } catch (ClosedSelectorException e) {
                // ignore
            } catch (Throwable e) {
                Log.err(e);
            }
        });

        client.connect(1000, "localhost", port);
    }

    @Override
    @SneakyThrows
    public void disconnect() {
        client.close();
    }

    @Override
    public void send(Object object) {
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
            Log.info("[Sock Client] Connected to server @. (@)", connection.getID(), connection.getRemoteAddressTCP());
        }

        @Override
        public void disconnected(Connection connection, DcReason reason) {
            Log.info("[Sock Client] Disconnected from server @. (@)", connection.getRemoteAddressTCP(), reason);
        }

        @Override
        public void received(Connection connection, Object object) {
            bus.fire(object);
        }
    }
}