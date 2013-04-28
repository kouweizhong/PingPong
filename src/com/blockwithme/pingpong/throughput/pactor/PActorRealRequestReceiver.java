package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.api.Actor;
import org.agilewiki.pactor.api.ResponseProcessor;

public interface PActorRealRequestReceiver extends Actor {
    public void processRequest(final PActorRealRequest request,
            final ResponseProcessor rp) throws Exception;
}
