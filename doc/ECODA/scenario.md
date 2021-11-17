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

####DSS Scenario Group#1: vertical scaling with optimisation of type 'resource'

initial configuration
start example-app-ve

***scenario#1.1*** - scaling UP for high resource usage with violation of SLAs of type 'active'

simulate high CPU usage with 'stress'

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

send a SLA violation about resources (sla_violation_sla_ve.json)

result: example-app-ve is scaled UP with +10% resources

***scenario#1.2*** - scaling DOWN for low resource usage

simulate low CPU usage (continues from scenario#1.1)

CPU usage is now low (the container with 'stress' was killed to spawn a new one with increased requests from scenario#1.1)
after some minutes, example-app-ve is scaled DOWN with -10% resources

***scenario#1.3*** - scaling UP for high resource usage with violation of SLAs of type 'active' WHEN a second violation of SLA of type 'ignore' is received. So, scaling UP is required 

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

first send a SLA violation about resources (sla_violation_sla_ve_ignore.json)
then send a SLA violation about resources (sla_violation_sla_ve.json)

result: example-app-ve is scaled UP with +10% resources

***scenario#1.4*** - skipping scaling UP for high resource usage with violation of SLAs of type 'active' WHEN a second violation of SLAs of type 'suspend' is received. So, scaling UP is NOT required 

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```

first send a SLA violation about resources (sla_violation_sla_ve_suspend.json)
then send a SLA violation about resources (sla_violation_sla_ve.json)

result: example-app-ve is scaled UP with +10% resources

####DSS Scenario Group#2: horizontal scaling with optimisation of type 'resource'

initial configuration
start example-app-ho

***scenario#2.1*** - scaling OUT for high resource usage based on violation of SLAs of type 'active'

```
APPNAME=example-app-ho
kubectl --kubeconfig kind-kubeconfig2.yaml exec -it `kubectl --kubeconfig kind-kubeconfig2.yaml get po -n testsp2 | grep $APPNAME | awk '{ print $1}'` -n testsp2 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ho.json)

result: example-app-ho is scaled OUT adding a replica

***scenario#2.2*** - scaling IN for low resource usage

simulate low CPU usage
stop the 'stress' tool

result: after some minutes, example-app-ho is scaled IN removing a replica

####DSS Scenario Group#3: Apps offloading to the cloud when scaling UP and edge resources are not available

initial configuration
start example-app-ve (cpu/mem requests are 250, edge node capacity is 300)

***scenario#3.1***
simulate high CPU usage with 'stress'

```
APPNAME=example-app-ve
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ve.json)

result#1: example-app-ve is scaled UP and stays on the edge. In fact, request are raised to 250+10%=275 which is still within the edge node capacity (300)

attach to the new POD and simulate high CPU usage with 'stress'

```
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ve.json)

result#2: example-app-ve is scaled UP and offloaded to the cloud. In fact, request are raised to 275+10%=302 which is too much for edge node capacity (300)

***scenario#3.2***

simulate low CPU usage

CPU usage is now low (the container with 'stress' was killed to spawn a new one with increased requests from scenario#1.1)
result: after some minutes, example-app-ve is scaled DOWN with -10% resources, fits the edge node capacity, so is offloaded to the edge

####DSS Scenario Group#4: Apps offloading on edge/cloud according to ECODA algorithm

initial configuration
start, sequentially, example-app-bash1, example-app-bash2, example-app-bash3, example-app-bash4
example-app-bash1+example-app-bash2 are instantiated on the edge
example-app-bash3+example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the ECODA spreadsheet
result: the ECODA edge/cloud offloads are coherent

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

***scenario#4.1***

stop example-app-bash1 (cpu/mem requests go to 0)

result: example-app-bash4 is moved to the cloud 

***scenario#4.2***

start example-app-bash1 (cpu/mem requests go to 250)

result: example-app-bash1 is started on the cloud, then is moved to the edge 

***scenario#4.3***

change example-app-bash3 priority to 2

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

***scenario#4.4***

change example-app-bash3 service requests to 100

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

####DSS Scenario Group#5: Apps offloading on edge/cloud according to ECODA algorithm plus increase/decrease of resource requests 

initial configuration
change ServiceOptimisation on Service example-app-bash1/2/3/4

start, sequentially, example-app-bash1, example-app-bash2, example-app-bash3, example-app-bash4
example-app-bash1 AND example-app-bash2 are instantiated on the edge
example-app-bash3 AND example-app-bash4 are instantiated on the cloud

***scenario#5.1***
on example-app-bash2
simulate high CPU usage

```
APPNAME=example-app-bash2
kubectl --kubeconfig kind-kubeconfig2.yaml exec -it `kubectl --kubeconfig kind-kubeconfig2.yaml get po -n testsp2 | grep $APPNAME | awk '{ print $1}'` -n testsp2 -- sh
simulate high CPU usage with 'stress'
stress --cpu 8
```

send a SLA violation about resources (sla_violation_bash2.json)
result: resource requests(300/300) go UP and the app offloaded to the cloud

on the other apps
result: see resource requests going DOWN

get the requests and put them in the ECODA spreadsheet
result: the ECODA edge/cloud offloads are coherent

