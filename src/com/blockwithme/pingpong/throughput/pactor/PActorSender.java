package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.ActorBase;
import org.agilewiki.pactor.ResponseProcessor;
import org.agilewiki.pautil.PAIterator;

/**
 * Test code.
 */
public class PActorSender extends ActorBase implements
        PActorRealRequestReceiver {

    private final PActorSimpleRequestReceiver echo;
    private final int count;
    private final int burst;

    public PActorSender(final PActorSimpleRequestReceiver echo,
            final int _count, final int _buffer) {
        this.echo = echo;
//        echo.setInitialBufferCapacity(b + 10);
        count = _count;
        burst = _buffer;
    }

    @Override
    public void processRequest(final PActorRealRequest unwrappedRequest,
            final ResponseProcessor rd1) throws Exception {
        final boolean real = unwrappedRequest != null;
        (new PAIterator() {
            int i;

            @Override
            public void process(final ResponseProcessor rd2) throws Exception {
                if (i > count)
                    rd2.processResponse(this);
                else {
                    i += 1;
                    final ResponseProcessor rd3 = new ResponseProcessor() {
                        int r = burst;

                        @Override
                        public void processResponse(
                                final Object unwrappedResponse)
                                throws Exception {
                            r -= 1;
                            if (r == 0) {
                                rd2.processResponse(null);
                            }
                        }
                    };
                    int j = 0;
                    while (j < burst) {
                        new PActorSimpleRequest(echo.getMailbox(), echo).send(
                                getMailbox(), rd3);
                        j += 1;
                    }
                }
            }
        }).iterate(rd1);
    }
}
