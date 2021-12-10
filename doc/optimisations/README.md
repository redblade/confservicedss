This folder contains the spreadsheet to replicate the ECODA, the TTODA (and so on) algorithms to check if the DSS is behaving as expected

Optimisations implemented so far are:
- "resource"
- "offloading"
- "scaling"
- "latency" aka ECODA
- "resource_latency" ECODA + "resource"
- "latency_faredge" TTODA
- "custom"

The **resource** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is useful in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical
The **offloading** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is useful in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical
The **scaling** optimisation adjusts the cpu/mem reserved resources depending on the actual resource usage of a Service and on the SLA violations received. This optimisation is useful in case of edge and cloud infrastructures with very different cpu (mostly)/mem(less critical) performances and where latency is not critical

The **latency** optimisation implements the ECODA algorithm. This optimisation is useful in case of homogeneous edge and cloud infrastructure in terms of cpu/mem where latency is critical

The **resource_latency** optimisation implements both the "resource" and the "latency" optimisations together. This optimisation is useful in case of edge and cloud infrastructures with very different cpu/mem and where latency is critical

The **latency_faredge** optimisation implements the TTODA algorithm. This optimisation is useful in case of homogeneous far-edge, edge and cloud infrastructure in terms of cpu/mem where latency is critical
(**) Under development - TTODA algorithm not finalised and not publicly available yet

The **resource_latency_faredge** optimisation implements both the "resource" and the "latency_faredge" optimisations together. This optimisation is useful in case of far-edge, edge and cloud infrastructures with very different cpu/mem and where latency is critical
(**) Under development - TTODA algorithm not finalised and not publicly available yet

More details on the optimisations are provided in the optimisation_scenarios.md


