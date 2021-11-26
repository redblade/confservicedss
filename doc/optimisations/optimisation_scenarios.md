<H1> DSS optimisation scenarios </H1>

<h3> Note on Optimisation and SLA </h3>

Optimisations are of three types:

- 'resources': this optimisation manages resource allocation using requests limits
    - whenever a SLA violation is received about a SLA which is dependent on resources, if used resources are close to the request limits they are increased
    - if no SLA violations are received for some time and resources used are far below request limits, they are decreased.
    
- 'latency': this optimisation reduces an utility function according to the ECODA optimisation algorithm 
- 'resources_latency': this optimisation combines 'resources' and 'latency' together: requests limits are dynamically changed and ECODA is used to reduce latency

SLA are of three types:
- 'active': this SLA is considered as related to resource consumption, so resource increase is managed to reduce violations
- 'suspend': this SLA is considered as dependent on a major issue (eg., radio failure) which requires to suspend any resource increase/decrease on the related service until this SLA violation is solved
- 'ignore': this SLA is considered as NOT related to resource consumption, so it is ignored by the DSS

SLA Violations are considered as solved if no new violations from the same SLA are received within the monitoring period (eg., 1min)

SLA Violations are consumed by the DSS using Kafka: for the tests, they can be sent with:

```
./send_dev_kafka.sh -t sla_violation -f <my-file>
```

<h2> SETUP OF THE TEST ENVIRONMENT </h2>
Test environment is based on KinD - see the instructions to configure it in ../doc/kind/README.txt

Two Kubernetes infrastructures are configured:
- cluster1:
    - cluster1-master : cloud (unavailable for scheduling)
    - cluster1-worker : cloud (with cpu/mem 5000)
    - cluster1-worker2: edge  (with cpu/mem 300)
    - cluster1-worker3: edge  (with cpu/mem 300)

- cluster2:
    - cluster2-master : cloud  (unavailable for scheduling)
    - cluster2-worker : cloud  (with cpu/mem 3000)

<h2>  DSS SCENARIO GROUP#1: optimisation of type 'resource', horizontal scaling based on SLA violations received </h2>

initial configuration: App example-app-ve running

<u><b>scenario#1.1</b></u> - scaling UP because of high resource usage when SLA violations of type 'active' are received

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

<u><b>scenario#1.2</b></u> - scaling DOWN because of low resource usage after a grace period without SLA violations

a) simulate low CPU usage

Nothing to do. After scenario#1.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources

<u><b>scenario#1.3</b></u> - scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'ignore' was received. So, scaling UP is required. 

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

<u><b>scenario#1.4</b></u> - skipping scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'suspend' was received. So, scaling UP is NOT required 

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

initial configuration: App example-app-ho running

<u><b>scenario#2.1</b></u> - scaling OUT because of high resource usage when SLA violations of type 'active' are received

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

<u><b>scenario#2.2</b></u> - scaling IN because of low resource usage after a grace period without SLA violations

simulate low CPU usage stopping the 'stress' tool

result: after some minutes, example-app-ho is scaled IN, removing a replica

<h2> DSS SCENARIO GROUP#3: optimisation of type 'resource', offloading to the edge/cloud based on SLA violations received and resources available </h2> 

initial configuration: App example-app-ve running (cpu/mem requests are 250, edge node capacity is 300)

<u><b>scenario#3.1</b></u> - offloading to the cloud when scaling UP would be necessary BUT edge resources are not sufficient
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

<u><b>scenario#3.2</b></u> - offloading to the edge when resources are available again

simulate low CPU usage

Nothing to do. After scenario#3.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources, fits the edge node capacity, so is offloaded to the edge

<h2>  DSS SCENARIO GROUP#4: optimisation of type 'latency', offloading on edge/cloud based on ECODA optimisation </h2> 
initial configuration: App example-app-bash1, example-app-bash2, example-app-bash3, example-app-bash4 running

see that
example-app-bash1 and example-app-bash2 are instantiated on the edge
example-app-bash3 and example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the [ECODA spreadsheet](ECODA.ods)

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA edge/cloud offloads are coherent

<u><b>scenario#4.1</b></u>

stop example-app-bash1 (cpu/mem requests go to 0)

result: example-app-bash4 is moved to the cloud 

<u><b>scenario#4.2</b></u>

start example-app-bash1 (cpu/mem requests go to 250)

result: example-app-bash1 is started on the cloud, then is moved to the edge 

<u><b>scenario#4.3</b></u>

change example-app-bash3 priority to 2

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<u><b>scenario#4.4</b></u>

change example-app-bash3 service requests to 100

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<h2> DSS SCENARIO GROUP#5: optimisation of type 'resources_latency', offloading on edge/cloud based on SLA violations received, resource availability and ECODA optimisation </h2>

initial configuration: App example-app-bash1, example-app-bash2, example-app-bash3, example-app-bash4 running

see that
example-app-bash1 and example-app-bash2 are instantiated on the edge
example-app-bash3 and example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the ECODA spreadsheet in doc/ECODA

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA edge/cloud offloads are coherent

<u><b>scenario#5.1</b></u>

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

result#3: the ECODA edge/cloud offloads are coherent


