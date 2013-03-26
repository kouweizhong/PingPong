package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Test code.
 */
final public class Echo extends JLPCActor implements SimpleRequestReceiver {
    @Override
    public void processRequest(final SimpleRequest unwrappedRequest,
            final RP responseProcessor) throws Exception {
        responseProcessor.processResponse(null);
    }
}
