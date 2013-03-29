package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * A wrapper of an array of requests to be sent to multiple actors.
 */
public class JActorRunParallel extends Request<Object, JActorParallel> {
    /**
     * The wrapped requests;
     */
    private Request[] requests;

    /**
     * Returns the wrapped requests.
     *
     * @return The wrapped requests.
     */
    public Request[] getRequests() {
        return requests;
    }

    /**
     * Create the request.
     *
     * @param requests the requests to be run in parallel.
     */
    public JActorRunParallel(Request[] requests) {
        this.requests = requests;
    }

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof JActorParallel;
    }

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        ((JActorParallel) targetActor).runParallel(requests, rp);
    }
}
