package com.ospx.sock;

import arc.net.*;
import arc.struct.Seq;
import arc.util.Log;

public class SockNetListener implements NetListener {
    private final EndPoint socket;

    public SockNetListener(EndPoint socket) {
        this.socket = socket;
    }

    @Override
    public void connected(Connection connection) {
        if (!isServer()) {
            Log.info("Connected to the Sock server");
            return;
        }

        if (!connection.getRemoteAddressTCP().getAddress().isLoopbackAddress()) {
            connection.close(DcReason.closed);
            return;
        }

        Log.info("[Sock] @ has connected", connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection, DcReason reason) {
        if (!isServer()) return;

        Log.info("[Sock] Connection @ has been closed. Reason = @",
                connection.getRemoteAddressTCP(),
                reason);
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object == null) return;

        SockEvents.fire(object);

        if (socket instanceof Server server) {
            for (var other : server.getConnections()) {
                if (other == connection) continue;
                other.sendTCP(object);
            }
        }
    }

    public boolean isServer() {
        return socket instanceof Server;
    }
}