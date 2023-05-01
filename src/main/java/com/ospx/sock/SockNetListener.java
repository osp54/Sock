package com.ospx.sock;

import arc.net.*;
import arc.struct.Seq;
import arc.util.Log;

public class SockNetListener implements NetListener {
    private final EndPoint endPoint;
    public SockNetListener(EndPoint endPoint) {
        this.endPoint = endPoint;
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
        if (object instanceof SockEvent) {
            SockEvents.fire(object);
            if (endPoint instanceof Server) {
                var connections = Seq.with(((Server) endPoint).getConnections());
                connections.remove(connection);
                connections.each(conn -> conn.sendTCP(object));
            }
        }
    }

    public boolean isServer() {
        return endPoint instanceof Server;
    }
}
