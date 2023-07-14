package com.ospx.sock;

import arc.math.Mathf;
import arc.mock.MockApplication;
import arc.net.DcReason;
import arc.util.Log;
import com.ospx.sock.EventBus.*;

import static arc.Core.*;

public class Test {
    public static final ServerSock server = Sock.server(Mathf.random(9999));
    public static final ClientSock client = Sock.client(server.getPort());

    public static void main(String... args) {
        app = new MockApplication();

        server.connect();
        client.connect();

        server.on(Test1.class, test1 -> {
            Log.info("Received?");
            server.respond(test1, new Test2());
        });

        client.request(new Test1(), test2 -> {
            Log.info("Received!");
        });

        server.getServer().getConnections()[0].close(DcReason.timeout);

        while (true) {}
    }

    public static class Test1 extends Request<Test2> {}
    public static class Test2 extends Response {}
}