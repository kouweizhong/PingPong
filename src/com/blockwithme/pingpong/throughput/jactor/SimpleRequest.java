package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

public class SimpleRequest extends Request<Object, SimpleRequestReceiver> {
    public final static SimpleRequest req = new SimpleRequest();

    @Override
    public boolean isTargetType(final Actor targetActor) {
        return targetActor instanceof SimpleRequestReceiver;
    }

    @Override
    public void processRequest(final JLPCActor targetActor, final RP rp)
            throws Exception {
        final SimpleRequestReceiver smDriver = (SimpleRequestReceiver) targetActor;
        smDriver.processRequest(this, rp);
    }
}
