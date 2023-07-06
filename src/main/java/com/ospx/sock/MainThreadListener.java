package com.ospx.sock;

import arc.Core;
import arc.net.NetListener;

public class MainThreadListener extends NetListener.QueuedListener {

    public MainThreadListener(NetListener listener) {
        super(listener);
    }

    @Override
    protected void queue(Runnable runnable) {
        Core.app.post(runnable);
    }
    
}
