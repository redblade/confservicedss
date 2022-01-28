This file (and folder) contains the documentation about the DSS optimisations, some [scenarios](optimisation_scenarios.md) to replicate the algorithms and spreadsheets to validate the implementation of more complex algorithms (eg. [ECODA](https://www.techrxiv.org/articles/preprint/An_Optimization_Framework_for_Edge-to-Cloud_Offloading_of_Kubernetes_Pods_in_V2X_Scenarios/16725643/1), [TTODA](link_to_be_added_as_soon_as_the_paper_is_public)).

The DSS implements the following optimisations (see **eu.pledgerproject.confservice.optimisation** Java package for the code)
- "resources"
- "offloading"
- "scaling"
- "latency" (implements "ECODA" algorithm)
- "resources_latency" (combines "latency" and "resources")
- "latency_faredge" (implements "TTODA" algorithm)
- "resources_latency_faredge" (combines "latency_faredge" and "resources")
- "webhook"

**Main actions** taken by the DSS:
Note the "SLA monitoring period" mentioned below is the "monitoring.slaViolation.periodSec" in the SP preferences

- '**resources**': this optimisation changes the Apps *reserved resources* using the App configured **requests limits**. The goal is to reserve some resources to an App and check if they are really necessary: increase them if a SLA violation is received AND resource used are close to the resource reserved, decrease them if no SLA violations are received in the SLA monitoring period AND resources used are much lower than resource reserved. More in details:
    - whenever a SLA violation is received about a SLA which is dependent on resources, if used resources are **close to the resource limits** AND there has not been another scaling/offloading in the past SLA monitoring period, THEN resources are **increased**
    - if no SLA violations are received in the SLA monitoring period AND **the service was not scaled or offloaded in that period** AND if resources used are **far below resource limits**, they are **decreased**.
  Depending on the resources availability and the deployment preferences, the DSS:
    - scales App up/out (if resources need to be increased and there are resources available)
    - scales App down/in (if App is on the edge and resources need to be decreased)
    - offloads App from edge to cloud (if App is on the edge, resources need to be increased and there are no resources available on the edge)
    - offloads App from cloud to edge (if App is on the cloud, resources need to be decreased and there are now resources available on the edge)

-  '**offloading**': a simplified version of 'resources' optimisation where the Apps are offloaded to the cloud, then back to the edge depending on the SLA violations BUT without changing the reserved resources. The actual resource usage of the App is IGNORED.
-  '**scaling**': a simplified version of 'resources' optimisation where the Apps are scaled up/out, then scale down/in depending on the SLA violations BUT without changing the reserved resources. The actual resource usage of the App is IGNORED.
-  '**latency**': this optimisation reduces the App latency in a two-tier cloud-edge infrastructure using the ECODA  algorithm that is run periodically to check the parameters it relies on: reserved resources, priorities, startup times, cloud/edge resource capacity and cloud->edge latency
-  '**resources_latency**': this optimisation combines 'resources' and 'latency' together: **resource limits** are dynamically changed as in "resources" and ECODA is used to reduce latency
-  '**latency_faredge**': this optimisation reduces the App latency in a three-tier cloud-edge-faredge infrastructure using the TTODA algorithm that is run periodically to check the parameters it relies on: reserved resources, priorities, startup times, cloud/edge/faredge resource capacity and cloud->edge and edge->faredge latencies
-  '**resources_latency_faredge**': this optimisation combines 'resources' and 'latency_faredge' together: **resource limits** are dynamically changed as in "resources" and TTODA is used to reduce latency
-  '**webhook**': this invokes an external HTTP service to allow the implementation of custom optimisation algorithm. A NodeRed instance is configured in Pledger to receive webhooks from the DSS to facilitate the creation of custom algorithm using a visual tool for coding


**Recommended** scenario for each optimisation:

- **resources**:
    - infrastructure with **edge and cloud nodes**
    - scarce edge resources **managed by the DSS**
    - **very varied HW performance on the edge** (mostly cpu)
    - **latency is not critical**

- **offloading**:
    - infrastructure with **edge and cloud nodes**
    - scarce edge resources **managed by separate entity** and where lack of resources requires **offloading** to the cloud
    - **very varied HW performance on the edge** (mostly cpu)
    - **latency is not critical**

- **scaling**:
    - infrastructure with **edge and cloud nodes**
    - scarce edge resources **managed by separate entity** and where lack of resources requires **scaling up** on the edge
    - **very varied HW performance on the edge nodes** (mostly cpu)
    - **latency is not critical**

- **latency**:
    - infrastructure with **edge and cloud nodes**
    - **homogeneus HW performance on the edge nodes**
    - **latency is critical**

- **resources_latency**:
    - infrastructure with **edge and cloud nodes**
    - scarce edge resources **managed by the DSS**
    - **very varied HW performance on the edge nodes** (mostly cpu)
    - **latency is critical**

- **latency_faredge**:
    - infrastructure with **faredge, edge and cloud nodes**
    - **homogeneus HW performance on the faredge and edge nodes**
    - **latency is critical**
    
- **resources_latency_faredge**:
    - infrastructure with **faredge, edge and cloud nodes**
    - scarce edge resources **managed by the DSS**
    - **very varied HW performance on the faredge and edge nodes** (mostly cpu)
    - **latency is critical**
    
- **webhook**:
    - a **custom algorithm** is required



