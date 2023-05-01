package com.ospx.sock;

import arc.net.NetSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoBufferOverflowException;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PacketSerializer implements NetSerializer {
    private final Kryo kryo = new Kryo();

    public PacketSerializer() {
        kryo.setRegistrationRequired(false);
        kryo.setAutoReset(true);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    @Override
    public void write(ByteBuffer buffer, Object object) {
        var output = new ByteBufferOutput(ByteBuffer.allocate(8192));
        output.setBuffer(buffer);

        kryo.writeClass(output, object.getClass());
        kryo.writeObject(output, object);
        output.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(ByteBuffer buffer) {
        var input = new ByteBufferInput(buffer);
        var registration = kryo.readClass(input);

        if (registration == null) {
            return null;
        }

        input.close();
        return kryo.readObject(input, registration.getType());
    }
}
