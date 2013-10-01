package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Test code.
 */
final public class JActorEcho extends JLPCActor implements JActorSimpleRequestReceiver {
    @Override
    public void processRequest(final JActorSimpleRequest unwrappedRequest,
            final RP responseProcessor) throws Exception {
        responseProcessor.processResponse(null);
    }
}
