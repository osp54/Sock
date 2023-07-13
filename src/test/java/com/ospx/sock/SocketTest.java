package com.ospx.sock;

import arc.math.Mathf;
import arc.mock.MockApplication;
import arc.util.Timer;
import com.ospx.sock.EventBus.*;
import org.junit.*;

import static arc.Core.*;
import static org.junit.Assert.*;

public class SocketTest {
    public final ServerSock server = Sock.server(Mathf.random(9999));
    public final ClientSock client = Sock.client(server.getPort());

    @Before
    public void load() {
        app = new MockApplication();

        server.connect();
        client.connect();
    }

    @Test
    public void testConnected() {
        assertTrue("Server is always connected", server.isConnected());
        assertTrue("Client should be connected", client.isConnected());

        client.disconnect();
        assertFalse("Client should be disconnected", client.isConnected());

        client.connect();
        assertTrue("Client should be connected", client.isConnected());
    }

    @Test
    public void testRun() {
        server.run(123, () -> {});
        client.run("test", () -> {});

        assertTrue("The server should be subscribed to Integer.class", server.getBus().getEvents().containsKey(Integer.class));
        assertTrue("The client should be subscribed to String.class", client.getBus().getEvents().containsKey(String.class));
    }

    @Test
    public void testOn() {
        class Test1 {}
        class Test2 {}

        server.on(Test1.class, test1 -> {});
        client.on(Test2.class, test2 -> {});

        assertTrue("The server should be subscribed to Test1.class", server.getBus().getEvents().containsKey(Test1.class));
        assertTrue("The client should be subscribed to Test2.class", client.getBus().getEvents().containsKey(Test2.class));
    }

    @Test
    public void testRequest() throws Exception {
        class Test2 extends Response {
            public Test2(Request<?> request) {
                super(request);
            }
        }

        class Test1 extends Request<Test2> { }

        var subscription = client.on(Test1.class, test1 -> {
            client.send(new Test2(test1));
        });

        var request = new Test1();
        int id = request.getId();

        client.request(request, response -> {
            assertEquals("Request and Response IDs must be equal", id, response.getId());
        }, () -> {
            fail("Request should not be timed out");
        }, 1f);

        Thread.sleep(1000L);
        subscription.unsubscribe();

        client.on(Test1.class, test1 -> {
            Timer.schedule(() -> client.send(new Test2(test1)), 1f);
        });

        request = new Test1();
        client.request(request, response -> {
            fail("Request should not be timed out");
        }, () -> {}, 0.5f);

        Thread.sleep(1000L);
    }
}