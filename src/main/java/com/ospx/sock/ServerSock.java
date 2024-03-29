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
        this.server = new Server(65536, 32768, serializer);
        this.port = port;

        this.server.addListener(new MainThreadListener(new ServerSockListener()));
    }

    /**
     * Binds the server to a specified port
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
     * Closes all connections, then stops the server
     */
    @Override
    @SneakyThrows
    public void disconnect() {
        server.close();
    }

    /**
     * Fires all listeners, then sends an object to all clients
     */
    @Override
    public void send(Object value) {
        bus.fire(value);
        if (isConnected()) server.sendToAllTCP(value);
    }

    /**
     * The ServerSock is always connected, even if {@link Sock#connect()} wasn't called before
     */
    public boolean isConnected() {
        return super.isConnected();
    }

    public class ServerSockListener implements NetListener {
        @Override
        public void connected(Connection connection) {
            if (connection == null) {
                Log.debug("[Sock Server] Null connection obtained");
                return;
            }

            try {
                if (!connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
                    connection.close(DcReason.closed);
                    return;
                }
            } catch (Exception e) {
                Log.debug("[Sock Server] Null address obtained");
                Log.debug(e);
                return;
            }

            server.sendToTCP(connection.getID(), new SockName(name));
        }

        @Override
        public void disconnected(Connection connection, DcReason reason) {
            Log.info("[Sock Server] Client @ has disconnected. (@)", connection, reason);
        }

        @Override
        public void received(Connection connection, Object object) {
            if (object instanceof SockName name) {
                connection.setName(name.name());
                Log.info("[Sock Server] Client @ has connected. (@)", connection, connection.getRemoteAddressTCP());
                return;
            }

            bus.fire(object);
            server.sendToAllExceptTCP(connection.getID(), object);
        }
    }
}