# Pledger core components end-to-end scenarios involving ConfService and DSS

Note: actions listed with [A] are done automatically by the ConfService or by the DSS

### ConfService component

>Pledger components integrated: Benchmarking Suite, SLA Lite, Orchestrator


user roles:
- **administrators**: manages users and roles to access ConfService and DSS
- **infrastructure providers**: configure their infrastructures to make them available to the Service Providers
- **service providers**: configure their Apps, the related SLA and Guarantees with metrics and thresholds

#### administrators
##### user configuration activities
- create, manage, enable/disable users and their roles (API access, Service Provider, Infrastructure Provider)
- [A] In case of creation, ConfService automatically sends an email to the provided address to request for activation

##### log management activities
- check the system and audit logs, changes the logs level

#### infrastructure providers
##### configuration activities
1. configure their Infrastructure and Nodes 

    - [A] ConfService automatically extracts and adds "hardware label" to the Nodes automatically, if the Infrastructure supports it

2. label the Nodes with "domain labels" (eg. location, **security capabilities**)
3. configure whether he/she wants to benchmark an infrastructure
     - [A] ConfService automatically sends a msg on StreamHandler using Kafka to alert the other Pledger core components:
        - Benchmarking Suite: gets the Infrastructure and Nodes from the ConfService
        - SLA Lite: gets the Infrastructure and Nodes from the ConfService
        - Orchestrator:  gets the Infrastructure and Nodes from the ConfService

##### basic monitoring activities
- read the Benchmarks
- read the reports about Infrastructure, Node and Benchmark

#### service providers

##### configuration of App/SLA/Guarantee

1. configure **Projects** to define the quota available on a specific Infrastructure
2. configure **CatalogApps** with descriptors to be possibly used during the App creation stage
3. configure **Apps** either manually writing a descriptor or re-using existing one from a CatalogApp
4. configure **Apps**' **reserved resources** and initial parameters
     - [A] ConfService creates Service starting from the App descriptor (App can be made of multiple Services)
- configure **SLA** per Service, specifying the type: 
    - **active**: used by the DSS for Service resource management - a SLA violation of this type is expected to be generated by application metrics which depends on resource (cpu/mem)
    - **suspend**: used by the DSS to suspend Service resource management on the Service - a SLA violation of this type is expected to be generated when a major issue occurs and no resource increase would improve the Service behavior
    - **ignore**: not used by the DSS - a SLA violation of this type is ignored by the DSS, it could be used by other components (eg. smart contract)
- configure **Guarantees** for each SLA, with the **metric** to monitor and the different **thresholds** 
    - [A] After each entity creation/update, ConfService automatically sends a msg on StreamHandler using Kafka to alert the other Pledger core components:
       - SLA Lite: gets the Project, App, Service, SLA, Guarantee from the ConfService
- read the **ServiceReports**
- configures the **App profile** with labels


##### configuration of DSS activities

- configure the DSS **ServiceConstraints** for each Service to define the deployment priorities based on the Node "hardware" and "domain" labels
    - [A] the DSS create **DeploymentOptions** with different options where a Service can be deployed, with **ranking**

- configure the DSS **ServiceOptimisation** policy for each Service:
    - **scaling**: the DSS scales either horizontally or vertically based on the SLA violations received. More details in the DSS component.
    - **offloading**: the DSS offloads to the cloud or to the edge based on the SLA violations received. More details in the DSS component.
    - **resource**: the DSS dynamically changes the Service reserved resources. More details in the DSS component.
    - **latency**: the DSS optimises the edge-to-cloud latency according to ECODA algorithm
    - **resources_latency**: the DSS dynamically changes the Service resource reserved AND also optimises the edge-to-cloud latency according to ECODA algorithm 
    - **webhook**: the DSS invokes an external URL when a violation is received to allow custom automations


## DSS component

>Pledger components integrated: ConfService, Orchestrator, Benchmarking, MonitoringEngine, AppProfiler, SLA Lite
   
##### App start/stop activities

- Service provider starts/stops an App
    - [A] the DSS sends messages on Kafka about App start/stop for the **Orchestrator**

##### monitoring activities
- [A] the DSS synchronizes with the **ConfService** and uses the configuration data stored
- [A] the DSS receives messages on Kafka, by the **Benchmarking**, and stores Benchmarks and BenchmarkReports
- [A] the DSS receives messages on Kafka, by the **MonitoringEngine**, about system metrics and application resource metrics for Infrastructures which do not support Prometheus and stores them in NodeReport and ServiceReport
- [A] the DSS extracts system metrics and application resource metrics from Kubernetes through metrics-server and GoldPinger and store them in NodeReport and ServiceReport
- [A] the DSS receives messages on Kafka, by the **AppProfiler**, about the best Benchmark for a given Service and stores its name in the Service profile 
- [A] the DSS receives messages on Kafka, by the **SLA Lite**, with SLA Violations and categorizes them


##### optimisation activities
- [A] the DSS, for SLA of type **active** and for which the Service has not received violations on SLA of type **suspend** for some time (*), decides whether to activate and **directly scale up/out** or **directly offload to the cloud** or **increase reserved resources** or not depending on the **actual Service resource usage** being close to the Service reserved resources configured, or takes no action
- [A] the DSS, for Services that have not received SLA violations, decides whether to activate and **scale down/in** or **offload to the edge** or **reduce reserved resources** or takes no actions
- [A] the DSS optimises Services based on their ServiceOptimisation configured:
    - **resource**: it changes the Service reserved resources values like follows: 
       - if the decision is to **increase reserved resources** and the current DeploymentOptions has enough resources, depending on the **severity score** it triggers a scaling up/out, otherwise it triggers an offloading to a worse ranking (edge to cloud). Both actions send messages on Kafka for the **Orchestrator**
       - if the decision is to **decrease reserved resources** and there is a better DeploymentOptions with enough resources, depending on the **severity score** it triggers an offload (cloud to edge), otherwise it triggers a scaling down/in. Both actions send messages on Kafka for the **Orchestrator**
    - **scaling**: it directly scales up/out or down/in as with **resource** BUT without dynamically changing the resources reserved
    - **offloading**: it directly offloads to the cloud or edge as with **resource** BUT without dynamically changing the resources reserved
    - **latency**: periodically checks the ECODA monitoring metrics. Depending on the new deployment plan updates and on the **severity score**, the DSS triggers the offloading sending messages on Kafka for the **Orchestrator**
    - **resources_latency**: works as with **resource** and also checking the ECODA monitoring metrics. Eventually the DSS triggers the scaling/offloading sending messages on Kafka for the **Orchestrator**
    - **webhook**: invokes an external URL to allow custom automation
- [A] the DSS, when offloading, decides the Nodes where to run the Service. If more than one are available with the same **severity score**, it uses Benchmarks (with the best "performance_index" metric) to choose the Node
- [A] the DSS, to choose which Benchmark to use for a given Service, in case no Benchmark name is configured for a Service (by the AppProfiler), matches the App labels with the Benchmark ones


An example of the DSS "resource" optimisation scenario is reported below:

1.	an App is configured with a Service with an SLA on an application metric (eg. “parts produced”) that depends on the compute resources (cpu, mem)
2.	the Service is configured with initial reserved resources (eg. 200 millicore + 200 MB)
3.	the Service initially consumes much less than the reserved resources (eg. 70 millicore + 70 MB)
4.	compute load is simulated on the App (eg. connect to the App and use “stress” tool)
5.	the Service consumed resources are now close to reserved resources (eg. 190 millicore + 190 MB)
6.	SLA Lite sends an SLA Violation about the application metric (i.e. on “parts produced”)
7.	the DSS increases the reserved resources: depending on the Service configuration and on the resource availability on the Nodes, it either scales up/out or offloads to the cloud
8.	After some time without SLA Violations on that Service, the DSS either scales down/in or offloads to the edge


An example of the DSS "latency" optimisation scenario is reported below:
1. there are cloud and edge infrastructures available
2. some Apps are configured with Services to be run possibly on the edge as a first option. Each Service has an amount of reserved resources (cpu, mem) which is fixed. Not all Services can fit into the edge infrastructure.
3. the Services are launched, Service priorities and their key monitoring metrics are monitored and used to place the Service on the edge or on the cloud


An example of the DSS "resources_latency" optimisation scenario is a combination on the two above:
1. start with the "latency" scenario, then DSS also changes reserved resources values, which are used for the placement on the edge and cloud


