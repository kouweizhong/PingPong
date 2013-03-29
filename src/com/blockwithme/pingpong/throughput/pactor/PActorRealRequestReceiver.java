package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.Actor;
import org.agilewiki.pactor.ResponseProcessor;

public interface PActorRealRequestReceiver extends Actor {
    public void processRequest(final PActorRealRequest request,
            final ResponseProcessor rp) throws Exception;
}
