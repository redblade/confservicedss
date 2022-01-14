This folder contains the following data:

- architecture: documentation about ConfServiceDSS architecture and the Pledger core components scenarios. It also highlight the dependencies with the other components and how to use only ConfServiceDSS to test the DSS optimisations.


- env: environment variables used for the correct execution of ConfServiceDSS


- kafka: scripts to send Kafka messages to the DSS. This way, we can simulate SLA violations so that it is not necessary to integrate with SLALite and a separate component using the same data model for the messages can be used.


- kind: documentation and scripts to setup some test environments with KinD to test DSS optimisations. This way, it is possible to test the DSS optimisation in a Kubernetes multi-node infrastructure just using Docker. This allows to test the Kubernetes API without the need to play with a real infrastructure


- optimisations: documentation about the optimisations implemented by the DSS. You can find there the different algorithms implemented and the scenarios which fit most


- test: documentation for integration tests of ConfService and DSS with Pledger components, like the Benchmarking suite. It is mostly for internal use.


- tools: documentation and scripts to integrate ConfService and DSS with other tools. This includes, for example, scripts to show reports in Grafana, notes for the configuration of Kubernetes, MySQL, and so on. It is mostly for internal use.