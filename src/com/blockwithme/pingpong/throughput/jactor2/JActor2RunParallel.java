package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.messaging.AsyncRequest;
import org.agilewiki.jactor2.core.processing.MessageProcessor;

/**
 * A wrapper of an array of requests to be sent to multiple actors.
 */
public class JActor2RunParallel extends AsyncRequest<Object> {
    /**
     * The wrapped requests;
     */
    private final AsyncRequest[] requests;

    private final JActor2Parallel targetActor;

    /**
     * Returns the wrapped requests.
     *
     * @return The wrapped requests.
     */
    public AsyncRequest[] getRequests() {
        return requests;
    }

    /**
     * Create the request.
     *
     * @param requests the requests to be run in parallel.
     */
    public JActor2RunParallel(final MessageProcessor targetMailbox,
            final JActor2Parallel _targetActor, final AsyncRequest[] requests) {
        super(targetMailbox);
        this.requests = requests;
        targetActor = _targetActor;
    }

    @Override
    public void processAsyncRequest() throws Exception {
        targetActor.runParallel(requests, this);
    }
}
