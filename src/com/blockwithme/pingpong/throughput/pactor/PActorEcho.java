package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.api.ActorBase;
import org.agilewiki.pactor.api.ResponseProcessor;

/**
 * Test code.
 */
final public class PActorEcho extends ActorBase implements
        PActorSimpleRequestReceiver {
    @Override
    public void processRequest(final PActorSimpleRequest unwrappedRequest,
            final ResponseProcessor responseProcessor) throws Exception {
        responseProcessor.processResponse(null);
    }
}
