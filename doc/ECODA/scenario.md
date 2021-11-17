# DSS optimisation scenarios

### TEST ENVIRONMENT
KinD based - see doc/kind/README.txt

Two Kubernetes infrastructures are configured:
- cluster1:
    - cluster1-master : cloud (unavailable for scheduling)
    - cluster1-worker : cloud (with cpu/mem 5000)
    - cluster1-worker2: edge  (with cpu/mem 300)
    - cluster1-worker3: edge  (with cpu/mem 300)

- cluster2:
    - cluster2-master : edge  (unavailable for scheduling)
    - cluster2-worker : edge  (with cpu/mem 300)

SLA are of three types:
- 'active':  this SLA is considered as related to resource consumption, so resource increase is necessary to reduce/mitigate violations
- 'suspend': this SLA is considered as a major issue (eg., radio failure) which requires to suspend any resource increase on the related service
- 'ignore':  this SLA is considered as NOT related to resource consumption, so resource increase is not needed to reduce/mitigate violations

SLA Violations can be sent over Kafka using
./send_dev_kafka.sh -t sla_violation -f <my-file>

### DSS Scenario group#1, optimisation of type 'resource', scaling based on SLA violations received

initial configuration: App example-app-ve running

<u>***scenario#1.1***</u> - scaling UP because of high resource usage when SLA violations of type 'active' are received

a) simulate high CPU usage with 'stress'

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

b) send a SLA violation about resources (sla_violation_sla_ve.json)

result: example-app-ve is scaled UP with +10% resources

<u>***scenario#1.2***</u> - scaling DOWN because of low resource usage after a grace period without SLA violations

a) simulate low CPU usage

Nothing to do. After scenario#1.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources

<u>***scenario#1.3***</u> - scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'ignore' was received. So, scaling UP is required. 

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

first send a SLA violation about resources (sla_violation_sla_ve_ignore.json)
then send a SLA violation about resources (sla_violation_sla_ve.json)

result: example-app-ve is scaled UP with +10% resources

<u>***scenario#1.4***</u> - skipping scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'suspend' was received. So, scaling UP is NOT required 

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

first send a SLA violation about resources (sla_violation_sla_ve_suspend.json)
then send a SLA violation about resources (sla_violation_sla_ve.json)

result: example-app-ve is NOT scaled UP

### DSS Scenario group#2: horizontal scaling with optimisation of type 'resource'

initial configuration: App example-app-ho running

<u>***scenario#2.1***</u> - scaling OUT because of high resource usage when SLA violations of type 'active' are received

```
APPNAME=example-app-ho
kubectl --kubeconfig kind-kubeconfig2.yaml exec -it `kubectl --kubeconfig kind-kubeconfig2.yaml get po -n testsp2 | grep $APPNAME | awk '{ print $1}'` -n testsp2 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ho.json)

result: example-app-ho is scaled OUT adding a replica

<u>***scenario#2.2***</u> - scaling IN because of low resource usage after a grace period without SLA violations

simulate low CPU usage
stop the 'stress' tool

result: after some minutes, example-app-ho is scaled IN, removing a replica

### DSS Scenario group#3: offloading to the cloud

initial configuration: App example-app-ve running (cpu/mem requests are 250, edge node capacity is 300)

<u>***scenario#3.1***</u> - offloading to the cloud when scaling UP would be necessary BUT edge resources are not sufficient
simulate high CPU usage with 'stress'

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ve.json)

result#1: example-app-ve is scaled UP and stays on the edge. In fact, request are raised to 250+10%=275 which is still within the edge node capacity (300)

attach to the new POD and simulate again high CPU usage with 'stress'

```
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send another SLA violation about resources (sla_violation_sla_ve.json)

result#2: example-app-ve is scaled UP and offloaded to the cloud. In fact, request are raised to 275+10%=302 which is too much for edge node capacity (300)

<u>***scenario#3.2***</u> - offloading to the edge when resources are available again

simulate low CPU usage

Nothing to do. After scenario#3.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources, fits the edge node capacity, so is offloaded to the edge

### DSS Scenario group#4: Apps offloading on edge/cloud using ECODA optimisation algorithm
initial configuration: start example-app-bash1, example-app-bash2, example-app-bash3, example-app-bash4

see that
example-app-bash1+example-app-bash2 are instantiated on the edge
example-app-bash3+example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the ECODA spreadsheet in doc/ECODA

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA edge/cloud offloads are coherent

<u>***scenario#4.1***</u>

stop example-app-bash1 (cpu/mem requests go to 0)

result: example-app-bash4 is moved to the cloud 

<u>***scenario#4.2***</u>

start example-app-bash1 (cpu/mem requests go to 250)

result: example-app-bash1 is started on the cloud, then is moved to the edge 

<u>***scenario#4.3***</u>

change example-app-bash3 priority to 2

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<u>***scenario#4.4***</u>

change example-app-bash3 service requests to 100

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

### DSS Scenario group#5: Apps offloading on edge/cloud using ECODA optimisation algorithm plus increase/decrease of resource requests based on SLA Violations

initial configuration: start example-app-bash1, example-app-bash2, example-app-bash3, example-app-bash4

see that
example-app-bash1+example-app-bash2 are instantiated on the edge
example-app-bash3+example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the ECODA spreadsheet in doc/ECODA

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA edge/cloud offloads are coherent

<u>***scenario#5.1***</u>

on example-app-bash2 simulate high CPU usage

```
APPNAME=example-app-bash2
kubectl --kubeconfig kind-kubeconfig2.yaml exec -it `kubectl --kubeconfig kind-kubeconfig2.yaml get po -n testsp2 | grep $APPNAME | awk '{ print $1}'` -n testsp2 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```

send a SLA violation about resources (sla_violation_bash2.json)
result: resource requests(300/300) go UP and the app offloaded to the cloud

on the other apps
result#1: see resource requests going DOWN

get the requests and put them in the ECODA spreadsheet

result#2: the ECODA edge/cloud offloads are coherent

