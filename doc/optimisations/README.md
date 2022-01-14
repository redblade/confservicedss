This folder contains the documentation about the DSS optimisations and some spreadsheets to validate the implementation of most complex algorithm (eg. ECODA, TTODA).

The DSS implements the following optimisations:
- "resource"
- "offloading"
- "scaling"
- "latency" (implements "ECODA" algorithm)
- "resource_latency" (combines "ECODA" and "resource")
- "latency_faredge" (implements "TTODA" algorithm)
- "resource_latency_faredge" (combines "TTODA" and "resource")
- "webhook"

The optimisations scenarios and tests are described in the [optimisation_scenarios.md](optimisation_scenarios.md) file

The best scenarios for each optimisation iFor each optimisation are reported below:

- The **resource** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is recommended in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical.

- The **offloading** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is recommended in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical.

- The **scaling** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is recommended in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical.

- The **latency** optimisation implements the "ECODA" algorithm. This optimisation is recommended in case of homogeneous edge and cloud infrastructure in terms of cpu/mem where latency is critical.

- The **resource_latency** optimisation implements both the "resource" and the "latency" optimisations together. This optimisation is recommended in case of edge and cloud infrastructures with very different cpu/mem and where latency is critical.

- The **latency_faredge** optimisation implements the "TTODA" algorithm. This optimisation is recommended in case of homogeneous far-edge, edge and cloud infrastructure in terms of cpu/mem where latency is critical.

- The **resource_latency_faredge** optimisation implements both the "resource" and the "latency_faredge" optimisations together. This optimisation is recommended in case of far-edge, edge and cloud infrastructures with very different cpu/mem and where latency is critical.

- The **webhook** optimisation just executes a HTTP call to an external service. This is recommended in case a custom algorithm is necessary. A NodeRed instance is configured in Pledger to receive webhooks from the DSS to facilitate the creation of custom algorithm using a visual tool for coding.


