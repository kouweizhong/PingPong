package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.apc.APCRequestSource;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;
import org.agilewiki.jactor.lpc.TargetActor;

/**
 * Test code.
 */
public final class JActorEcho extends JLPCActor implements TargetActor {
    private static class JActorSimpleRequest extends
            Request<Object, JActorEcho> {
        private static final JActorSimpleRequest req = new JActorSimpleRequest();

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return targetActor instanceof JActorEcho;
        }

        @Override
        public void processRequest(final JLPCActor targetActor, final RP rp)
                throws Exception {
            final JActorEcho smDriver = (JActorEcho) targetActor;
            smDriver.processRequest(this, rp);
        }
    }

    public void processRequest(final JActorSimpleRequest unwrappedRequest,
            final RP responseProcessor) throws Exception {
        // Real request processing would go here
        responseProcessor.processResponse(null);
    }

    public void sendSimpleRequest(final APCRequestSource source, final RP rp)
            throws Exception {
        JActorSimpleRequest.req.send(source, this, rp);
    }
}
