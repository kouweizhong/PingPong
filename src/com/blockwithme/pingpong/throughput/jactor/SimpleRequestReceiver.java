package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.TargetActor;

public interface SimpleRequestReceiver extends TargetActor {
    public void processRequest(final SimpleRequest request, final RP rp)
            throws Exception;
}
