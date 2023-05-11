package com.ospx.sock;

import arc.net.FrameworkMessage;
import arc.net.NetSerializer;
import arc.util.Log;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class PacketSerializer implements NetSerializer {
    public final Kryo kryo = new Kryo();

    public PacketSerializer() {
        kryo.setAutoReset(true);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    @Override
    public void write(ByteBuffer buffer, Object object) {
        try (ByteBufferOutput output = new ByteBufferOutput(buffer)) {
            kryo.writeClass(output, object.getClass());
            kryo.writeObject(output, object);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object read(ByteBuffer buffer) {

        var d = buffer.array();
        Log.info(Arrays.toString(d));
        if (d[0] == 0&&(d[1] >=0&&d[1] <=4)) {
            return readFramework(buffer);
        }

        try (ByteBufferInput input = new ByteBufferInput(buffer)) {
            var registration = kryo.readClass(input);
            if (registration == null) return null;

            return kryo.readObject(input, registration.getType());
        }catch (Exception e){
            Log.err(e);
            return null;
        }
    }

    public FrameworkMessage readFramework(ByteBuffer buffer){
        buffer.position(0);
        buffer.get();
        byte id = buffer.get();
        Log.info(id);
        if(id == 0){
            FrameworkMessage.Ping p = new FrameworkMessage.Ping();
            p.id = buffer.getInt();
            p.isReply = buffer.get() == 1;
            return p;
        }else if(id == 1){
            return FrameworkMessage.discoverHost;
        }else if(id == 2){
            buffer.get();
            buffer.get();
            return FrameworkMessage.keepAlive;
        }else if(id == 3){
            FrameworkMessage.RegisterUDP p = new FrameworkMessage.RegisterUDP();
            p.connectionID = buffer.getInt();
            return p;
        }else if(id == 4){
            FrameworkMessage.RegisterTCP p = new FrameworkMessage.RegisterTCP();
            p.connectionID = buffer.getInt();
            return p;
        }else{
            throw new RuntimeException("Unknown framework message!");
        }
    }
}