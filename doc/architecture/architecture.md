## Confservice and DSS architecture and dependencies with other Pledger core components

This file introduces the ConfService and DSS architecture with respect to the [Pledger](http://www.pledger-project.eu/) core components. An overview of the Pledger core components **end-to-end scenarios** involving ConfService and DSS is provided in [this document](Pledger_core_scenarios.md)

ConfService and DSS are two functional components which are released in a three layers architecture:
- frontend, based on [Angular](https://angular.io)
- backend, based on [SpringBoot](https://spring.io/projects/spring-boot)
- persistent data storage, currently [MySQL](https://www.mysql.com), could be any [JPA supported](https://en.wikibooks.org/wiki/Java_Persistence/Databases) RDBMS.

ConfService and DSS define **three roles**:
- Administrators: responsible for the creation of other users and the audit logs
- Infrastructure Providers: responsible for the configuration of Infrastructures where to run applications
- Service Providers: responsible for the configuration and execution of applications on the infrastructures

**ConfService main role** is to allow users to configure the data needed by the DSS depending on their **role**:
- Administrator: manages the **creation and removal of other users** and the audit logs
- InfrastructureProvider: **configures infrastructures** where to run applications, with the entities Infrastructure and Node
- ServiceProvider: **configures the applications** to run and their SLA, with the entities App, Service, SLA, Guarantee

The main configuration entities are:
- **Infrastructure**: represents a logical set of Nodes that can host Apps/Services
- **Node**: represents the unit that can host a Service and has a type and labels to allow the definition of deployment preferences
- **App**: represents the unit that can be started or stopped as a whole. Each App is made of one or more Services
- **Service**: represents the unit that is executed and contains a type and a descriptor of process to launch. For example a Kubernetes deployment
- **SLA**: represents the set of agreements on metrics values that a specific Service needs to fulfil. Each agreement is a Guarantee. Each Service can have one or more SLAs.
- **Guarantee**: represents an agreement on one metric and contains one or more thresholds with different severity
- **SLA Violation**: represents a violation of a Guarantee of a specific SLA on a specific Service
- **Project**: represents a contract that allows a ServiceProvider to access an Infrastructure (eg. with a quota)

The conceptual data model is represented in this [UML diagram](data_model.png). The complete UML class diagram is represented in [this picture](jhipster-jdl.png)


**DSS main role** is to autonomously trigger placing and scaling of Apps on the infrastructure in order to:
- minimise SLA violations
- optimise resource usage on the edge
- minimise latency using optimisation algorithms.

Instantiation is triggered by the ConfService (manually) or by the DSS (automatically) and is managed through messages sent to the StreamHandler using Kafka protocol.

To take its decisions and have them applied, the DSS needs: 
1. to interact with the Orchestrator component to apply deployment changes; 
2. to retrieve monitoring data about Apps and Infrastructure usage from the Monitoring Engine component;
3. to retrieve SLA violations coming from SLA Lite component. 

In Pledger, all the data above is shared through the StreamHandler component using the Kafka protocol. The high-level workflow with Pledger dependencies is shown in [this picture](confservice_dss.drawio.png).

A more detailed picture with the all the Pledger components involved is shown in [this picture](sp_full_demo.drawio.png)

To **facilitate the system integrators** and to **reduce the dependencies on other Pledger components**, the ConfService and DSS also offer direct support to:
1. Kafka, to communicate without the need to have the **StreamHandler**
2. Kubernetes, to directly orchestrate Pods without the need to have the **E2CO Orchestrator**
3. Prometheus AlertManager (work in progress) to get SLA violations, without the need to have the **SLA Lite**. Currently, SLA violations over App metrics can be sent using **bash scripts** (see **doc/kafka** folder).
4. Kubernetes metrics-server and GoldPinger services for the system and application metrics retrieval, without the need to have the **MonitoringEngine**

The high-level workflow with no dependencies on other Pledger components is shown in [this picture](confservice_dss_no_pledger.drawio.png) used for DSS the integration tests described in the **doc/optimisations** folder and allow their replication using KinD environments (see **doc/kind** folder)
