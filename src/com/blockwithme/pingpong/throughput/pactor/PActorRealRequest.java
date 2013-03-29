package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.RequestBase;
import org.agilewiki.pactor.ResponseProcessor;

public class PActorRealRequest extends RequestBase<Object> {

    private final PActorRealRequestReceiver target;

    public PActorRealRequest(final Mailbox targetMailbox,
            final PActorRealRequestReceiver _target) {
        super(targetMailbox);
        target = _target;
    }

    @Override
    public void processRequest(final ResponseProcessor<Object> responseProcessor)
            throws Exception {
        target.processRequest(this, responseProcessor);
    }
}
