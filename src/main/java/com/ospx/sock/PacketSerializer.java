package com.ospx.sock;

import arc.net.FrameworkMessage;
import arc.net.NetSerializer;
import arc.util.Log;

import arc.net.FrameworkMessage.*;
import com.alibaba.fastjson.JSON;

import java.nio.ByteBuffer;

public class PacketSerializer implements NetSerializer {
    private final EventBus bus;
    public PacketSerializer(EventBus bus) {
        this.bus = bus;
    }
    @Override
    public void write(ByteBuffer buffer, Object object) {
        if(object instanceof FrameworkMessage m){
            buffer.put((byte)-2);
            writeFramework(buffer,m);
        }else {
            buffer.put((byte) -1);
            writeString(buffer, object.getClass().getName());

            byte[] json = JSON.toJSONBytes(object);
            buffer.putInt(json.length);
            buffer.put(json);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object read(ByteBuffer buffer) {
        byte type = buffer.get();

        if (type==-2){
            return readFramework(buffer);
        }else{
            String classString = readString(buffer);

            try {
                Class<?> clazz = Class.forName(classString);

                if (!bus.contains(clazz)) {
                    bypassReadLimit(buffer);
                    return null;
                }

                int jsonLength = buffer.getInt();

                byte[] jsonBytes = new byte[jsonLength];
                buffer.get(jsonBytes);

                return JSON.parseObject(jsonBytes, clazz);
            } catch (ClassNotFoundException e) {
                Log.err("[Sock] Not found class " + classString, e);
                bypassReadLimit(buffer);
                return null;
            }
        }
    }

    private void writeString(ByteBuffer buffer, String string) {
        buffer.putInt(string.length());
        buffer.put(string.getBytes());
    }
    private String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] bytes = new byte[length];
        buffer.get(bytes);

        return new String(bytes);
    }

    private void writeFramework(ByteBuffer buffer, FrameworkMessage message){
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

    private FrameworkMessage readFramework(ByteBuffer buffer){
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

    private void bypassReadLimit(ByteBuffer buffer) {
        buffer.position(buffer.position() + buffer.remaining());
    }
}