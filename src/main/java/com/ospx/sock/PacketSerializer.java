package com.ospx.sock;

import arc.net.FrameworkMessage;
import arc.net.NetSerializer;
import arc.util.Log;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import org.objenesis.strategy.StdInstantiatorStrategy;

import arc.net.FrameworkMessage.*;

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
        if(object instanceof FrameworkMessage m){
            buffer.put((byte)-2);
            writeFramework(buffer,m);
        }else {
            buffer.put((byte) -1);
            try (ByteBufferOutput output = new ByteBufferOutput(buffer)) {
                kryo.writeClass(output, object.getClass());
                kryo.writeObject(output, object);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object read(ByteBuffer buffer) {
        byte type = buffer.get();

        if (type==-2){
            return readFramework(buffer);
        }else{
            try (ByteBufferInput input = new ByteBufferInput(buffer)) {
                var registration = kryo.readClass(input);
                if (registration == null) return null;
                Log.debug("<<< "+registration.getType().getName());
                return kryo.readObject(input, registration.getType());
            }
        }
    }

    public void writeFramework(ByteBuffer buffer, FrameworkMessage message){
        if(message instanceof Ping p){
            buffer.put((byte)0);
            buffer.putInt(p.id);
            buffer.put(p.isReply ? 1 : (byte)0);
        }else if(message instanceof DiscoverHost){
            buffer.put((byte)1);
        }else if(message instanceof KeepAlive){
            buffer.put((byte)2);
        }else if(message instanceof RegisterUDP p){
            buffer.put((byte)3);
            buffer.putInt(p.connectionID);
        }else if(message instanceof RegisterTCP p){
            buffer.put((byte)4);
            buffer.putInt(p.connectionID);
        }
    }

    public FrameworkMessage readFramework(ByteBuffer buffer){
        byte id = buffer.get();

        if(id == 0){
            Ping p = new Ping();
            p.id = buffer.getInt();
            p.isReply = buffer.get() == 1;
            return p;
        }else if(id == 1){
            return FrameworkMessage.discoverHost;
        }else if(id == 2){
            return FrameworkMessage.keepAlive;
        }else if(id == 3){
            RegisterUDP p = new RegisterUDP();
            p.connectionID = buffer.getInt();
            return p;
        }else if(id == 4){
            RegisterTCP p = new RegisterTCP();
            p.connectionID = buffer.getInt();
            return p;
        }else{
            throw new RuntimeException("Unknown framework message!");
        }
    }
}