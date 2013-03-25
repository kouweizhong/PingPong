PingPong
========

Shows how to implement a simple request-reply example using multiple Java Actor APIs, as a benchmark.

This doesn't try to be a perfect benchmark, and does not claim to be meaningful for your own use-case.

Also, doing throughput benchmarks is a lot harder, so we might not do that in the near future.

The goal is to measure *latency*, not throughput. That is why I do sequential request/reply cycles.

The worlkflow goes like this:

1. The "main" (unit test) creates and starts the pinger and ponger actors.
2. The "main" sends the "hammer request" to the pinger.
3. The pinger starts to hammer the ponger.
4. The pinger sends a ping to the ponger, waits for the pong.
5. Repeat #4 1 million time.
6. Send # of cycles back to "main".
7. Done

We have 3 warm-up rounds, and 10 measured rounds, which are averaged. Some variants/APIs take much longer, so expect 10 to 20 minutes runtime. The total runtime will also increase as we add new APIs.

We use http://labs.carrotsearch.com/junit-benchmarks.html for the benchmark implementation. It will create an HTML result file in the "charts" directory. I haven't worked out how to visualize it locally, due to some JavaScript issue. So it needs to be delivered by a real webserver.

We are currently testing JActors, PActors, Akka, JetLang and simple threads. In some cases, multiple variants are used.

We have a have-finished Kilim implementation. Lack of Maven support and the bytecode weaving requirement makes things a lot more difficult then for the other APIs.
Thsi seem to work: java -classpath lib/kilim-0.7.jar kilim.tools.Weaver target/classes -d target/kilim
But needs to be correctly integrated in the POM.

And maybe we will add Groovy actors too. But we don't plan to add anything that had no update in the last year or so.

Discussion forum:

* https://groups.google.com/forum/#!forum/agilewikidevelopers

Please post any questions, comments or patch there.

Current results, on my aging Intel Xeon X3360 @2.83 GHz, with 800 MHz RAM, Win7 PC, on Java 7, can be seen here:

http://skunkiferous.github.com/PingPong/

Be warned that I have teamed up with the author of JActor to create a full POJO reimplementation (PActor), and so of course those are the APIs for which we can create the most adequate test code. Still, you can see the code to make sure that we did approximatly the same thing for all APIs. Feel free to offer improvements to the other API test code, as we are trying to be fair.

