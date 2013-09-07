package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.ActorBase;
import org.agilewiki.jactor2.core.messaging.ResponseProcessor;

/**
 * Test code.
 */
final public class JActor2Echo extends ActorBase implements
        JActor2SimpleRequestReceiver {
    @Override
    public void processRequest(final JActor2SimpleRequest unwrappedRequest,
            final ResponseProcessor responseProcessor) throws Exception {
        responseProcessor.processResponse(null);
    }
}
