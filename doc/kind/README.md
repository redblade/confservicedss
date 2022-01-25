This folder contains folders to setup Kubernetes environment to demonstrate the DSS algorithms.

Two options are provided: one for [KinD](https://kind.sigs.k8s.io/) to test Kubernetes with Docker, one for real Kubernetes clusters.

####HOW to setup and configure KinD-based environment####

This option is ideal to test DSS with an average laptop (quad-core Intel I5 CPU, 16 GB RAM, 10 GB disk space) with Docker.

Two environments are provided, please use the corresponding sub-folder with instructions:

- **"cloud-edge"**: two-tier cloud-edge infrastructures to test the DSS optimisations:
    - "resource"
    - "offload"
    - "scaling"
    - "latency" (aka "ECODA")
    - "resources_latency" (which combines "resource" with "ECODA")    
    
    
- **"cloud-edge-faredge"**: three-tier cloud-edge-faredge infrastructures to test DSS optimisations:
     - "latency_faredge" (aka "TTODA")
     - "resources_latency_faredge" (which combines "resource" with "TTODA")    
    
####HOW to configure an existing Kubernetes cluster as a test environment####
    
For **real Kubernetes clusters**, please follow the instructions above and just **skip the KinD installation**. Then, change the Infrastructure and Node entities with the correct values as follows:
- Infrastructure: kubeconfig path, endpoint, properties, resources
- Node: node_type, properties, resources
    
In case you want to configure more Nodes, please make sure to:

1) have the following in the ConfService Node "properties" field to let the DSS understand which nodes are cloud, edge or faredge:
- if cloud:   'node_type': 'cloud'
- if edge:    'node_type': 'edge'
- if faredge: 'node_type': 'faredge'

2) have the correct entries and priorities in the ConfService "Service Constraint" entity to privilege deployment on faredge/edge nodes over cloud

3) configure the quotas in the ConfService "Project" entity

For more information and examples, please refer to the demos on Pledger YouTube channel

