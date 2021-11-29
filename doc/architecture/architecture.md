## Confservice and DSS architecture within Pledger project

This file introduces the ConfService and DSS architecture with respect to the [Pledger project](http://www.pledger-project.eu/) core components. 

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
- minimise latency using ECODA optimisation algorithm.

Instantiation is triggered by the ConfService (manually) or by the DSS (automatically) and is managed through messages sent to the StreamHandler using Kafka protocol.

To take its decisions and have them applied, the DSS needs: 
1. to interact with the Orchestrator component to apply deployment changes; 
2. to retrieve monitoring data about Apps and Infrastructure usage from the Monitoring Engine component;
3. to retrieve SLA violations coming from SLA Lite component. 

In Pledger, all the data above is shared through the StreamHandler component using the Kafka protocol. The high-level architecture is shown in [this picture](confservice_dss.drawio.png)

To facilitate the system integrators activities and limit the dependencies to run demos, the ConfService and DSS also offer direct support to some specific infrastructures, in particular:
1. support to opensource Kafka, without the need to have the **StreamHandler**
2. support to Kubernetes, without the need to have the **Orchestrator**
3. support (work in progress) to Prometheus AlertManager to get SLA violations, without the need to have the **SLA Lite** 
4. support to Kubernetes metrics-server and Goldpinger services for the system and application metrics retrieval, without the need to have the **MonitoringEngine**

More details for the system integrators demo are provided in the **"doc/kind"** folder