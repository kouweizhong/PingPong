package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.api.Actor;
import org.agilewiki.pactor.api.ResponseProcessor;

public interface PActorSimpleRequestReceiver extends Actor {
    public void processRequest(final PActorSimpleRequest request,
            final ResponseProcessor rp) throws Exception;
}
