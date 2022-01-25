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
    

NOTE: the above mentioned test environments are ideal to test DSS optimisations in small Kubernetes clusters that can be easily created on an average laptop (quad-core Intel I5 CPU, 16 GB RAM, 10 GB disk space) with Docker.
Of course, it is possible to use a real Kubernetes cluster. In this case, please make sure to have enough nodes:
- for cloud-edge tests: at least one cloud and one edge node
- for cloud-edge-faredge: at least one cloud, one edge and one faredge node

To configure a real Kubernetes cluster, please add "Infrastructure" and "Node" ConfService entities, then add the following property to the Node "properties":
- for cloud nodes: 'node_type': 'cloud'
- for edge nodes: 'node_type': 'edge'
- for faredge nodes: 'node_type': 'faredge'

Also, please make sure to configure the correct priorities in the "Service Constraint" ConfService entity to privilege some nodes (eg., faredge over edge over cloud).
For more information, please refer to the demos on YouTube or to the cloud-edge or cloud-edge-faredge instructions which refer to SQL scripts that can be used as an example.

