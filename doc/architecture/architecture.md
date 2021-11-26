##Confservice and DSS architecture

This file introduces the ConfService and DSS architecture with respect to the other [Pledger project](http://www.pledger-project.eu/) components. 

Confservice and DSS are two functional components which are released as a unique service made of three layers:
- frontend, based on [Angular](https://angular.io)
- backend, based on [SpringBoot](https://spring.io/projects/spring-boot)
- persistent data storage, currently [MySQL](https://www.mysql.com), could be any [JPA](https://en.wikibooks.org/wiki/Java_Persistence/Databases) RDBMS.

<b>Confservice main role</b> is to allow Service providers (also known as Software as a Service providers) and Infrastructure providers (also known as Infrastructure as a Service providers) to configure their infrastructures, Apps and the preferences to guide the deployment on the infrastructure.

Confservice supports Apps as a way to identify applications to run. Each App is made of multiple Services.

<b>DSS main role</b> is to autonomously trigger placing and scaling of Apps on the infrastructure in order to:
- minimise SLA violations
- optimise resource usage on the edge
- minimise latency using ECODA optimisation algorithm.

Instantiation is triggered by the Confservice (manually) or by the DSS (automatically) and is managed through messages sent to the StreamHandler using Kafka protocol.

To take its decisions and have them applied, the DSS needs: 
1. to interact with the Orchestrator to apply deployment changes; 
2. to retrieve monitoring data about Apps and Infrastructure usage from the Monitoring Engine;
3. to retrieve SLA violations coming from the SLA Manager. 

In Pledger, all the data above is shared through the StreamHandler component using the Kafka protocol. The architecture is shown in the [picture](confservicedss.drawio.png)

To facilitate the system integrator activities and limit the dependencies to run demos, the Confservice and DSS also offer direct support to some specific infrastructures, in particular:
1. direct support to Kubernetes, without the need to have the Orchestrator running.
2. support (work in progress) to integrate with Prometheus AlertManager to get SLA violations, without the need to have the SLA Manager running.
3. direct support to Kubernetes metrics-server and Goldpinger services for the App Infrastructure and App metrics retrieval, without the need to have the Monitoring Engine running.

