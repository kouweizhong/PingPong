PingPong
========

Shows how to implement a simple request-reply example using multiple Java Actor APIs, as a benchmark.

The goal is to measure *latency*, not throughput. That is why I do sequential request/reply cycles.

In particular, JActors, PActors, Akka, and simple threads. More to come.

In particular, we would like submissions for:

* http://www.malhar.net/sriram/kilim/
* http://code.google.com/p/jetlang/
* http://code.google.com/p/korus/

And we would accept submissions for other actor frameworks, that we do not know about.

Current results, on my aging Intel Xeon X3360 @2.83 GHz, with 800 MHz RAM, PC, can be seen here:

http://htmlpreview.github.com/?https://raw.github.com/skunkiferous/PingPong/master/results/PingPongBenchmarks.html
