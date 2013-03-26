package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.TargetActor;

public interface RealRequestReceiver extends TargetActor {
    public void processRequest(RealRequest request, RP rp)
            throws Exception;
}
