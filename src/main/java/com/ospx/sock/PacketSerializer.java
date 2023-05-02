package com.ospx.sock;

import arc.net.NetSerializer;
import arc.util.serialization.Json;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoBufferOverflowException;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;
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
        try (var input = new ByteBufferInput(buffer)) {
            var type = kryo.readClass(input);
            if (type == null) return null;

            return kryo.readObject(input, type.getType());
        }
    }
}