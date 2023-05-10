package com.ospx.sock;

import arc.net.FrameworkMessage.RegisterTCP;
import arc.net.NetSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.nio.ByteBuffer;

public class PacketSerializer implements NetSerializer {
    public final Kryo kryo = new Kryo();

    public PacketSerializer() {
        kryo.setAutoReset(true);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    @Override
    public void write(ByteBuffer buffer, Object object) {
        try (var output = new ByteBufferOutput(buffer)) {
            kryo.writeClass(output, object.getClass());
            kryo.writeObject(output, object);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object read(ByteBuffer buffer) {
        var duplicate = buffer.duplicate();
        if (duplicate.get() == -2)
            return register(duplicate);

        try (var input = new ByteBufferInput(buffer)) {
            var registration = kryo.readClass(input);
            if (registration == null) return null;

            return kryo.readObject(input, registration.getType());
        }
    }

    public Object register(ByteBuffer buffer) {
        if (buffer.get() == 4)
            return new RegisterTCP() {{
                connectionID = buffer.getInt();
            }};
        else return null;
    }
}