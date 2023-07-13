package com.ospx.sock;

import arc.net.NetListener;
import arc.net.NetListener.QueuedListener;

import static arc.Core.*;

public class MainThreadListener extends QueuedListener {

    public MainThreadListener(NetListener listener) {
        super(listener);
    }

    @Override
    protected void queue(Runnable runnable) {
        app.post(runnable);
    }
}