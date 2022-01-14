This file (and folder) contains the documentation about the DSS optimisations, some [scenarios](optimisation_scenarios.md) to replicate the algorithms and some spreadsheets to validate the implementation of most complex algorithm (eg. [ECODA](ECODA.ods), [TTODA](TTODA.ods)).

The DSS implements the following optimisations (see **eu.pledgerproject.confservice.optimisation** Java package for the code)
- "resources"
- "offloading"
- "scaling"
- "latency" (implements "ECODA" algorithm)
- "resources_latency" (combines "latency" and "resources")
- "latency_faredge" (implements "TTODA" algorithm)
- "resources_latency_faredge" (combines "latency_faredge" and "resources")
- "webhook"

Main actions taken in each optimisation algorithm:

- '**resources**': this optimisation changes the Apps *reserved resources* using the App **requests limits**, like follows:
    - whenever a SLA violation is received about a SLA which is dependent on resources; if used resources are **close to the resource limits** they are **increased**
    - if no SLA violations are received for some time; if resources used are **far below resource limits**, they are **decreased**.
  Depending on the resources availability and the deployment preferences, the DSS:
    - scales App up/out (if resources need to be increased and there are resources available)
    - scales App down/in (if App is on the edge and resources need to be decreased)
    - offloads App from edge to cloud (if App is on the edge, resources need to be increased and there are no resources available on the edge)
    - offloads App from cloud to edge (if App is on the cloud, resources need to be decreased and there are now resources available on the edge)

-  '**offloading**': a simplified version of 'resource' optimisation where the Apps are offloaded to the cloud, then back to the edge depending on the SLA violations BUT without changing the reserved resources. The actual resource usage of the App is IGNORED.
-  '**scaling**': a simplified version of 'resource' optimisation where the Apps are scaled up/out, then scale down/in depending on the SLA violations BUT without changing the reserved resources. The actual resource usage of the App is IGNORED.
-  '**latency**': this optimisation reduces the App latency in a two-tier cloud-edge infrastructure using the ECODA  algorithm that is run periodically to check the parameters it relies on: reserved resources, priorities, startup times, cloud/edge resource capacity and cloud->edge latency
-  '**resources_latency**': this optimisation combines 'resources' and 'latency' together: **resource limits** are dynamically changed as in "resources" and ECODA is used to reduce latency
-  '**latency_faredge**': this optimisation reduces the App latency in a three-tier cloud-edge-faredge infrastructure using the TTODA algorithm that is run periodically to check the parameters it relies on: reserved resources, priorities, startup times, cloud/edge/faredge resource capacity and cloud->edge and edge->faredge latencies
-  '**resources_latency_faredge**': this optimisation combines 'resources' and 'latency_faredge' together: **resource limits** are dynamically changed as in "resources" and TTODA is used to reduce latency
-  '**webhook**': this invokes an external HTTP service to allow the implementation of custom optimisation algorithm.


Here are reported the recommended scenarios for each optimisation:

- The **resources** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is **recommended** in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical.

- The **offloading** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is **recommended** in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical.

- The **scaling** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is **recommended** in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical.

- The **latency** optimisation implements the "ECODA" algorithm. This optimisation is **recommended** in case of homogeneous edge and cloud infrastructure in terms of cpu/mem where latency is critical.

- The **resources_latency** optimisation combines both the "resources" and the "latency" optimisations together. This optimisation is **recommended** in case of edge and cloud infrastructures with very different cpu/mem and where latency is critical.

- The **latency_faredge** optimisation implements the "TTODA" algorithm. This optimisation is **recommended** in case of homogeneous far-edge, edge and cloud infrastructure in terms of cpu/mem where latency is critical.

- The **resources_latency_faredge** optimisation combines both the "resources" and the "latency_faredge" optimisations together. This optimisation is **recommended** in case of far-edge, edge and cloud infrastructures with very different cpu/mem and where latency is critical.

- The **webhook** optimisation just executes a HTTP call to an external service. This is **recommended** in case a custom algorithm is necessary. A NodeRed instance is configured in Pledger to receive webhooks from the DSS to facilitate the creation of custom algorithm using a visual tool for coding.


