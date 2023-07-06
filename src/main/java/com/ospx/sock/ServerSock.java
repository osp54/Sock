package com.ospx.sock;

import arc.Core;
import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.net.Server;
import arc.util.Log;
import arc.util.Threads;
import lombok.SneakyThrows;

import java.nio.channels.ClosedSelectorException;

public class ServerSock extends Sock {

    public final Server server;
    public final int port;

    public ServerSock(int port) {
        this.server = new Server(32768, 16384, this.getPacketSerializer());
        this.port = port;

        this.server.addListener(new MainThreadListener(new ServerSockListener()));
    }

    @Override
    @SneakyThrows
    public void connect() {
        server.bind(port);
        this.thread = Threads.daemon("Sock Server", () -> {
            try {
                server.run();
            } catch (Throwable e) {
                if (!(e instanceof ClosedSelectorException))
                    Log.err(e);
            }
        });
    }

    @Override
    @SneakyThrows
    public void disconnect() {
        server.close();
    }

    @Override
    public void sendEvent(Object object) {
        bus.fire(object);
        server.sendToAllTCP(object);
    }

    public class ServerSockListener implements NetListener {

        @Override
        public void connected(Connection connection) {
            if (!connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
                connection.close(DcReason.closed);
                return;
            }

            Log.info("[Sock] Sock client @ has connected. (@)", connection.getID(), connection.getRemoteAddressTCP());
        }

        @Override
        public void disconnected(Connection connection, DcReason reason) {
            Log.info("[Sock] Sock client @ has disconnected due to @.", connection.getID(), reason);
        }

        @Override
        public void received(Connection connection, Object object) {
            bus.fire(object);
            server.sendToAllExceptTCP(connection.getID(), object);
        }
    }
}