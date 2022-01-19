This folder contains folders to setup the following test environments based on [KinD](https://kind.sigs.k8s.io/) for running local Kubernetes clusters using Docker container nodes:

- **"cloud-edge"**: KinD environment with two-tier cloud-edge infrastructures to test optimisations of type:
    - "resource"
    - "offload"
    - "scaling"
    - "latency" (aka "ECODA")
    - "resources_latency" (which combines "resource" with "ECODA")    
    
    
- **"cloud-edge-faredge"**: KinD environment with  three-tier cloud-edge-faredge infrastructures to test optimisations of type:
     - "latency_faredge" (aka "TTODA")
     - "resources_latency_faredge" (which combines "resource" with "TTODA")    
    
