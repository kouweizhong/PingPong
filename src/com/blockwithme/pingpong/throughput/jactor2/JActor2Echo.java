package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.ActorBase;
import org.agilewiki.jactor2.core.messaging.AsyncResponseProcessor;

/**
 * Test code.
 */
final public class JActor2Echo extends ActorBase implements
        JActor2SimpleRequestReceiver {
    @Override
    public void processRequest(final JActor2SimpleRequest unwrappedRequest,
            final AsyncResponseProcessor responseProcessor) throws Exception {
        responseProcessor.processAsyncResponse(null);
    }
}
