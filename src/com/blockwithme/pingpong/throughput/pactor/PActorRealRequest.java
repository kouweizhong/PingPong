package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.ResponseProcessor;
import org.agilewiki.pactor.Transport;
import org.agilewiki.pactor.UnboundRequestBase;

public class PActorRealRequest extends
        UnboundRequestBase<Object, PActorRealRequestReceiver> {
    public final static PActorRealRequest req = new PActorRealRequest();

    @Override
    public void processRequest(final PActorRealRequestReceiver _targetActor,
            final Transport<Object> responseProcessor) throws Exception {
        _targetActor.processRequest(this, responseProcessor);
    }
}
