This folder contains the spreadsheet to replicate the ECODA, the TTODA (and so on) algorithms implemented in the DSS

So far, the following optimisations are available:
- "resource"
- "offloading"
- "scaling"
- "latency" (aka "ECODA")
- "resource_latency" (ECODA + "resource")
- "latency_faredge" (aka TTODA)
- "resource_latency_faredge" (TTODA + "resource")
- "webhook"

The **resource** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is useful in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical
The **offloading** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is useful in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical
The **scaling** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is useful in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical

The **latency** optimisation implements the ECODA algorithm. This optimisation is useful in case of homogeneous edge and cloud infrastructure in terms of cpu/mem where latency is critical

The **resource_latency** optimisation implements both the "resource" and the "latency" optimisations together. This optimisation is useful in case of edge and cloud infrastructures with very different cpu/mem and where latency is critical

The **latency_faredge** optimisation implements the TTODA algorithm. This optimisation is useful in case of homogeneous far-edge, edge and cloud infrastructure in terms of cpu/mem where latency is critical

The **resource_latency_faredge** optimisation implements both the "resource" and the "latency_faredge" optimisations together. This optimisation is useful in case of far-edge, edge and cloud infrastructures with very different cpu/mem and where latency is critical

The optimisations scenarios and tests are described in the optimisation_scenarios.md file


