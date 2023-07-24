package com.ospx.sock;

import arc.net.*;
import arc.util.*;
import lombok.*;

import java.nio.channels.ClosedSelectorException;

@Getter
public class ClientSock extends Sock {
    private final Client client;
    private final int port;

    private boolean wasConnected;

    public ClientSock(int port) {
        this.client = new Client(32768, 16384, getSerializer());
        this.port = port;

        this.client.addListener(new MainThreadListener(new ClientSockListener()));

        Timer.schedule(() -> {
            Log.info("[Sock Client] Trying to reconnect to Sock server...");

            try {
                if (wasConnected && !isConnected())
                    connect();
            } catch (Throwable e) {
                Log.err(e);
            }
        }, 30f, 30f);
    }

    /**
     * Connects to the server on a specified port
     */
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

        wasConnected = true;
        client.connect(5000, "localhost", port);
    }

    /**
     * Disconnects from the server
     */
    @Override
    @SneakyThrows
    public void disconnect() {
        wasConnected = false;
        client.close();
    }

    /**
     * Fires all listeners, then sends an object to the server
     */
    @Override
    public void send(Object value) {
        getBus().fire(value);
        if (isConnected()) client.sendTCP(value);
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
            Log.info("[Sock Client] Disconnected from server @. (@)", connection.getID(), reason);
        }

        @Override
        public void received(Connection connection, Object object) {
            getBus().fire(object);
        }
    }
}