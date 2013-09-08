package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.Actor;
import org.agilewiki.jactor2.core.messaging.AsyncResponseProcessor;

public interface JActor2RealRequestReceiver extends Actor {
    public void processRequest(final JActor2RealRequest request,
            final AsyncResponseProcessor rp) throws Exception;
}
