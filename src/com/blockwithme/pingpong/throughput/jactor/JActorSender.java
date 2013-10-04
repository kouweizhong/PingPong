package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.JAIterator;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Test code.
 */
public class JActorSender extends JLPCActor implements
        JActorRealRequestReceiver {

    private final JActorEcho echo;
    private final int count;
    private final int burst;

    public JActorSender(final JActorEcho echo, final int _count,
            final int _burst) {
        this.echo = echo;
        echo.setInitialBufferCapacity(_burst + 10);
        count = _count;
        burst = _burst;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void processRequest(final JActorRealRequest unwrappedRequest,
            final RP rd1) throws Exception {
        (new JAIterator() {
            int i;

            @SuppressWarnings("unchecked")
            @Override
            public void process(final RP rd2) throws Exception {
                if (i > count)
                    rd2.processResponse(this);
                else {
                    i += 1;
                    final RP rd3 = new RP() {
                        int r = burst;

                        @Override
                        public void processResponse(
                                final Object unwrappedResponse)
                                throws Exception {
                            r -= 1;
                            if (r == 0)
                                rd2.processResponse(null);
                        }
                    };
                    int j = 0;
                    while (j < burst) {
                        echo.sendSimpleRequest(JActorSender.this, rd3);
                        j += 1;
                    }
                }
            }
        }).iterate(rd1);
    }
}
