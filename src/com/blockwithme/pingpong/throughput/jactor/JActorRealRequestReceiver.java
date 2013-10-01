package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.TargetActor;

public interface JActorRealRequestReceiver extends TargetActor {
    public void processRequest(JActorRealRequest request, RP rp)
            throws Exception;
}
