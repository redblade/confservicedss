<H1> DSS optimisation scenarios </H1>

Notes on Optimisation, SLA and test environment types

Optimisations are:

- 'resource': this optimisation changes the Apps *reserved resources* using the App **requests limits**, like follows:
    - whenever a SLA violation is received about a SLA which is dependent on resources; if used resources are **close to the resource limits** they are **increased**
    - if no SLA violations are received for some time; if resources used are **far below resource limits**, they are **decreased**.
  
  Depending on the resources availability and the deployment preferences, the DSS:
    - scales App up/out (if resources need to be increased and there are resources available)
    - scales App down/in (if App is on the edge and resources need to be decreased)
    - offloads App from edge to cloud (if App is on the edge, resources need to be increased and there are no resources available on the edge)
    - offloads App from cloud to edge (if App is on the cloud, resources need to be decreased and there are now resources available on the edge)

- 'offloading': a simplified version of 'resource' optimisation where the Apps are offloaded to the cloud, then back to the edge depending on the SLA violations BUT without changing the reserved resources. The actual resource usage of the App is IGNORED.
- 'scaling': a simplified version of 'resource' optimisation where the Apps are scaled up/out, then scale down/in depending on the SLA violations BUT without changing the reserved resources. The actual resource usage of the App is IGNORED.
- 'latency': this optimisation reduces the App latency in a two-tier cloud-edge infrastructure using the ECODA  algorithm that is run periodically to check the parameters it relies on: reserved resources, priorities, startup times, cloud/edge resource capacity and cloud->edge latency
- 'resource_latency': this optimisation combines 'resources' and 'latency' together: **resource limits** are dynamically changed as in "resource" and ECODA is used to reduce latency
- 'latency_faredge': this optimisation reduces the App latency in a three-tier cloud-edge-faredge infrastructure using the TTODA algorithm that is run periodically to check the parameters it relies on: reserved resources, priorities, startup times, cloud/edge/faredge resource capacity and cloud->edge and edge->faredge latencies
- 'resource_latency_faredge': this optimisation combines 'resources' and 'latency_faredge' together: **resource limits** are dynamically changed as in "resource" and TTODA is used to reduce latency


SLA are:
- 'active': this SLA is considered as related to resource consumption, so resource increase is managed to reduce violations
- 'suspend': this SLA is considered as dependent on a major issue (eg., radio failure) which requires to suspend any resource increase/decrease on the related service until this SLA violation is solved
- 'ignore': this SLA is considered as NOT related to resource consumption, so it is ignored by the DSS

SLA Violations are considered as "solved" if no new violations from the same SLA are received within the monitoring period (eg., 1min)

SLA Violations are sent by the SLA Manager and consumed by the DSS using Kafka: for the integration tests, they are **sent with a script**:


```
./send_dev_kafka.sh -t sla_violation -f <my-file>
```

Test environments are:

- cloud-edge: used to test almost all the optimisations above with the exception of the ****faredge** ones, using a mixed cloud-edge infrastructure based on Kubernetes
- cloud-edge-faredge: used to test the ****faredge** optimisations above, using a mixed cloud, edge and far-edge infrastructure based on Kubernetes


<h2> SETUP OF THE TEST CLOUD-EDGE ENVIRONMENT </h2>

This test environment is based on KinD - see the instructions to configure it in ../doc/kind/cloud-edge/README.txt

This is used to test optimisations of type:
- 'resource'
- 'offloading'
- 'scaling'
- 'latency'
- 'resource_latency'
- 'resource_latency_faredge'


<h2>  DSS SCENARIO GROUP#1: optimisation of type 'resource', horizontal scaling based on SLA violations received </h2>

<u><b>initial configuration</b></u> 

App example-app-ve is running

<u><b>test scenario#1.1</b></u> - scaling UP because of high resource usage when SLA violations of type 'active' are received

a) simulate high CPU usage with 'stress'

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

b) send a SLA violation about resources (sla_violation_sla_ve.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve.json

```

result: example-app-ve is scaled UP with +10% resources

<u><b>test scenario#1.2</b></u> - scaling DOWN because of low resource usage after a grace period without SLA violations

a) simulate low CPU usage

Nothing to do. After scenario#1.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources

<u><b>test scenario#1.3</b></u> - scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'ignore' was received. So, scaling UP is required. 

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

first send a SLA violation about resources (sla_violation_sla_ve_ignore.json), then send a SLA violation about resources (sla_violation_sla_ve.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve_ignore.json
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve.json

```


result: example-app-ve is scaled UP with +10% resources

<u><b>test scenario#1.4</b></u> - skipping scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'suspend' was received. So, scaling UP is NOT required 

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

first send a SLA violation about resources (sla_violation_sla_ve_suspend.json) then send a SLA violation about resources (sla_violation_sla_ve.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve_suspend.json
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve.json

```

result: example-app-ve is NOT scaled UP

<h2>  DSS SCENARIO GROUP#2: optimisation of type 'resource', horizontal scaling based on SLA violations received </h2> 

<u><b>initial configuration</b></u> 

App example-app-ho is running

<u><b>test scenario#2.1</b></u> - scaling OUT because of high resource usage when SLA violations of type 'active' are received

```
APPNAME=example-app-ho
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n test1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ho.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ho.json
```
result: example-app-ho is scaled OUT adding a replica

<u><b>test scenario#2.2</b></u> - scaling IN because of low resource usage after a grace period without SLA violations

simulate low CPU usage stopping the 'stress' tool

result: after some minutes, example-app-ho is scaled IN, removing a replica

<h2> DSS SCENARIO GROUP#3: optimisation of type 'resource', offloading to the edge/cloud based on SLA violations received and resources available </h2> 

<u><b>initial configuration</b></u> 

App example-app-ve is running (cpu/mem requests are 250, edge node capacity is 300)

<u><b>test scenario#3.1</b></u> - offloading to the cloud when scaling UP would be necessary BUT edge resources are not sufficient
simulate high CPU usage with 'stress'

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ve.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve.json
```

result#1: example-app-ve is scaled UP and stays on the edge. In fact, request are raised to 250+10%=275 which is still within the edge node capacity (300)

attach to the new POD and simulate again high CPU usage with 'stress'

```
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send another SLA violation about resources (sla_violation_sla_ve.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve.json
```

result#2: example-app-ve is scaled UP and offloaded to the cloud on cluster2. In fact, request are raised to 275+10%=302 which is too much for edge node capacity (300)

<u><b>test scenario#3.2</b></u> - offloading to the edge when resources are available again

simulate low CPU usage

Nothing to do. After scenario#3.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources, fits the edge node capacity, so is offloaded to the edge

<h2>  DSS SCENARIO GROUP#4: optimisation of type 'latency', offloading on edge/cloud based on ECODA optimisation </h2> 

<u><b>initial configuration</b></u> 

The following Apps are running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4

see that
example-app-bash1 and example-app-bash2 are instantiated on the edge
example-app-bash3 and example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the [ECODA spreadsheet](ECODA.ods)

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA offloads are coherent

<u><b>test scenario#4.1</b></u>

stop example-app-bash1 (cpu/mem requests go to 0)

result: example-app-bash4 is moved to the cloud 

<u><b>test scenario#4.2</b></u>

start example-app-bash1 (cpu/mem requests go to 250)

result: example-app-bash1 is started on the cloud, then is moved to the edge 

<u><b>test scenario#4.3</b></u>

change example-app-bash3 priority to 2

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<u><b>test scenario#4.4</b></u>

change example-app-bash3 service requests to 100

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<h2> DSS SCENARIO GROUP#5: optimisation of type 'resources_latency', offloading on edge/cloud based on SLA violations received, resource availability and ECODA optimisation </h2>

<u><b>initial configuration</b></u> 

The following Apps are running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4

see that
example-app-bash1 and example-app-bash2 are instantiated on the edge
example-app-bash3 and example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the ECODA spreadsheet in doc/ECODA

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA offloads are coherent

<u><b>test scenario#5.1</b></u>

on example-app-bash2 simulate high CPU usage

```
APPNAME=example-app-bash2
kubectl --kubeconfig kind-kubeconfig2.yaml exec -it `kubectl --kubeconfig kind-kubeconfig2.yaml get po -n testsp2 | grep $APPNAME | awk '{ print $1}'` -n testsp2 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```

send a SLA violation about resources (sla_violation_bash2.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_bash2.json
```

result#1: resource requests(300/300) go UP and the app offloaded to the cloud


result#2: on the other apps see resource requests going DOWN

get the requests and put them in the ECODA spreadsheet

result#3: the ECODA offloads are coherent

<h2> SETUP OF THE TEST CLOUD-EDGE-FAREDGE ENVIRONMENT </h2>

This test environment is based on KinD - see the instructions to configure it in ../doc/kind/cloud-edge-faredge/README.txt

This is used to test optimisations of type:
- 'latency_faredge'
- 'resource_latency_faredge'

<h2>  DSS SCENARIO GROUP#6: optimisation of type 'latency_faredge', offloading on edge/cloud based on TTODA optimisation </h2> 

<u><b>initial configuration</b></u> 

The following Apps are running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4
- example-app-bash5
- example-app-bash6


<u><b>test scenario#6.1</b></u>

from ServiceReport, wait for startup time to be available
get the requests and put them in the [TTODA spreadsheet](TTODA.ods)

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the TTODA offloads are coherent

<h2>  DSS SCENARIO GROUP#7: optimisation of type 'resource_latency_faredge', offloading on edge/cloud based on SLA violations received, resource availability and TTODA optimisation </h2>


<u><b>initial configuration</b></u> 

The following Apps are running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4
- example-app-bash5
- example-app-bash6

<u><b>test scenario#7.1</b></u>

on example-app-bash2 simulate high CPU usage

```
APPNAME=example-app-bash2
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```

send a SLA violation about resources (sla_violation_bash2.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_bash2.json
```

result#1: resource requests(300/300) go UP and the app offloaded to the cloud

result#2: on the other apps see resource requests going DOWN

get the requests and put them in the TTODA spreadsheet

result#3: the TTODA offloads are coherent



**Note**: videos about these optimisations can be found on the official [Pledger YouTube channel](https://www.youtube.com/channel/UCXV6V9rJ0ZvWhXeoWvDsArQ)


