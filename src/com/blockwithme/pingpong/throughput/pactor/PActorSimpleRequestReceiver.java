package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.Actor;
import org.agilewiki.pactor.ResponseProcessor;

public interface PActorSimpleRequestReceiver extends Actor {
    public void processRequest(final PActorSimpleRequest request,
            final ResponseProcessor rp) throws Exception;
}
