package com.ospx.sock;

import arc.net.*;
import arc.util.*;
import lombok.*;

import java.nio.channels.ClosedSelectorException;

@Getter
public class ServerSock extends Sock {
    public final Server server;
    public final int port;

    public ServerSock(int port) {
        this.server = new Server(32768, 16384, getSerializer());
        this.port = port;

        this.server.addListener(new MainThreadListener(new ServerSockListener()));
    }

    /**
     * @implNote binds the server to a specified port
     */
    @Override
    @SneakyThrows
    public void connect() {
        Threads.daemon("Sock Server", () -> {
            try {
                server.run();
            } catch (ClosedSelectorException e) {
                // ignore
            } catch (Throwable e) {
                Log.err(e);
            }
        });

        server.bind(port);
    }

    /**
     * @implNote closes all connections, then stops the server
     */
    @Override
    @SneakyThrows
    public void disconnect() {
        server.close();
    }

    /**
     * @implNote fires all listeners, then sends an object to all clients
     */
    @Override
    public void send(Object value) {
        getBus().fire(value);
        if (isConnected()) server.sendToAllTCP(value);
    }

    public class ServerSockListener implements NetListener {
        @Override
        public void connected(Connection connection) {
            if (!connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
                connection.close(DcReason.closed);
                return;
            }

            Log.info("[Sock Server] Client @ has connected. (@)", connection.getID(), connection.getRemoteAddressTCP());
        }

        @Override
        public void disconnected(Connection connection, DcReason reason) {
            Log.info("[Sock Server] Client @ has disconnected. (@)", connection.getID(), reason);
        }

        @Override
        public void received(Connection connection, Object object) {
            getBus().fire(object);
            server.sendToAllExceptTCP(connection.getID(), object);
        }
    }
}