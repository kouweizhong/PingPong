package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.Actor;
import org.agilewiki.jactor2.core.messaging.ResponseProcessor;

public interface JActor2RealRequestReceiver extends Actor {
    public void processRequest(final JActor2RealRequest request,
            final ResponseProcessor rp) throws Exception;
}
