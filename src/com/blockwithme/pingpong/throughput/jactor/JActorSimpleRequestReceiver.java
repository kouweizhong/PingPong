package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.TargetActor;

public interface JActorSimpleRequestReceiver extends TargetActor {
    public void processRequest(final JActorSimpleRequest request, final RP rp)
            throws Exception;
}
