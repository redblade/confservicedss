<H1> DSS optimisation scenarios </H1>

This file describes **how to test the DSS optimisations**; this includes: 
- the setup of the test environment
- the simulation of SLA violations
- the tests to validate the DSS optimisation 

Videos about the optimisations can be found on the Pledger [official YouTube channel](https://www.youtube.com/channel/UCXV6V9rJ0ZvWhXeoWvDsArQ)


<h2>Note on how SLA violations are managed by the DSS</h2>
- 'active': this SLA is considered as related to resource consumption, so resource increase is managed to reduce violations
- 'suspend': this SLA is considered as dependent on a major issue (eg., radio failure) which requires to suspend any resource increase/decrease on the related service until this SLA violation is solved
- 'ignore': this SLA is considered as NOT related to resource consumption, so it is ignored by the DSS

SLA Violations are considered as "closed" if no new violations from the same SLA are received within the SLA monitoring period (by default, 180s) that **can be changed in the SP preferences** ("monitoring.slaViolation.periodSec"). 

As described in the [Pledger architecture](../architecture/architecture.md), SLA Violations are sent by the SLA Manager and consumed by the DSS through the StreamHandler using Kafka.

**To allow the testing of the DSS and cut down the dependencies**, you just need Kafka running and the following script to generate and send SLA violations:


```
./send_dev_kafka.sh -t sla_violation -f <my-file>
```

Please refer to the architecture documentation for more details.

<h2>How to setup the test environments</h2>

To test the DSS optimisations, it is possible to configure **two different test environments** based on KinD which come with some pre-configured Apps:

1) **cloud-edge** (see the [instructions](../kind/cloud-edge/README.md)). This is used for optimisations:
   - 'resources'
   - 'offloading'
   - 'scaling'
   - 'latency'
   - 'resources_latency'
   - 'resources_latency_energy' - TO BE UPDATED
   
   

2) **cloud-edge-faredge** (see the [instructions](../kind/cloud-edge-faredge/README.md)). This is used for optimisations:
   - 'latency_faredge'
   - 'resources_latency_faredge'


Please find below the scenarios to test the DSS optimisations.

<h2> DSS SCENARIO #1</h2>
<H4>Test environment: "cloud-edge"</h4>
<H4>Goal: show how Apps are vertically scaled based on SLA violations received </h4>
<u><b>initial configuration</b></u> 

App example-app-ve is first configured with vertical scaling, has "resources" Optimisation and is running

<u><b>test #1.1</b></u> - scaling UP because of high resource usage when SLA violations of type 'active' are received

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

<u><b>test #1.2</b></u> - scaling DOWN because of low resource usage after a grace period without SLA violations

a) simulate low CPU usage

Nothing to do. After scenario#1.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources

<u><b>test #1.3</b></u> - scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'ignore' was received. So, scaling UP is required. 

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

<u><b>test #1.4</b></u> - skipping scaling UP because of high resource usage when SLA violations of type 'active' are received, WHEN a previous SLA violation of type 'suspend' was received. So, scaling UP is NOT required 

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

<h2>  DSS SCENARIO #2:  </h2> 
<H4>Test environment: "cloud-edge"</h4>
<h4>Goal: show how Apps are horizontally scaled based on SLA violations received</h4>

<u><b>initial configuration</b></u> 

App example-app-ho is configured with horizontal scaling and "resources" Optimisation, started and running

<u><b>test #2.1</b></u> - scaling OUT because of high resource usage when SLA violations of type 'active' are received

```
APPNAME=example-app-ho
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n test1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
#simulate high CPU usage with 'stress'
stress --cpu 8
```
send a SLA violation about resources (sla_violation_sla_ho.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ho.json
```
result: example-app-ho is scaled OUT adding a replica

<u><b>test #2.2</b></u> - scaling IN because of low resource usage after a grace period without SLA violations

simulate low CPU usage stopping the 'stress' tool

result: after some minutes, example-app-ho is scaled IN, removing a replica

<h2> DSS SCENARIO #3:  </h2>
<H4>Test environment: "cloud-edge"</h4>
<H4>Goal: show how Apps are offloaded to the edge/cloud nodes based on SLA violations received and resources available </H4>

<u><b>initial configuration</b></u> 

App example-app-ve is configured with "resources" optimisation, vertical scaling, has cpu/mem requests are 250, is started and running. 
Please note edge edge nodes have cpu/mem capacity 300/300.

<u><b>test #3.1</b></u> - offloading to the cloud when scaling UP would be necessary BUT edge resources are not sufficient
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

At first, example-app-ve is scaled UP and stays on the edge. In fact, request are raised to 250+10%=275 which is still within the edge node capacity (300)

attach to the new POD and simulate again high CPU usage with 'stress'

```
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
stress --cpu 8
```
send another SLA violation about resources (sla_violation_sla_ve.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_ve.json
```

Then, example-app-ve is scaled UP and offloaded to the cloud on cluster2. In fact, request are raised to 275+10%=302 which is too much for edge node capacity (300)

<u><b>test #3.2</b></u> - offloading to the edge when resources on edge are available again

simulate low CPU usage

Nothing to do. After test#3.1, CPU usage goes to low values as the container with 'stress' running was killed to to spawn a new one with increased requests

result: after some minutes, example-app-ve is scaled DOWN with -10% resources, fits the edge node capacity, so is offloaded to the edge

<h2>  DSS SCENARIO #4 </h2> 
<H4>Test environment: "cloud-edge"</h4>
<H4>Goal: show how Apps are offloaded to edge/cloud based on ECODA optimisation score</H4>

<u><b>initial configuration</b></u> 

The following Apps are configured with "latency" optimisation, then started and running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4

see that
example-app-bash1 and example-app-bash2 are instantiated on the edge
example-app-bash3 and example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the [ECODA spreadsheet](ECODA.ods) and check the expected offloads, or check the "OptimisationReport" in the ConfServiceDSS UI

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

first result: the ECODA offloads are coherent with the spreadsheet or with the "OptimisationReport" in the ConfServiceDSS UI

<u><b>test #4.1</b></u>

stop example-app-bash1 (cpu/mem requests go to 0)

result: example-app-bash4 is moved to the cloud 

<u><b>test #4.2</b></u>

start example-app-bash1 (cpu/mem requests go to 250)

result: example-app-bash1 is started on the cloud, then is moved to the edge 

<u><b>test #4.3</b></u>

change example-app-bash3 priority to 2

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<u><b>test #4.4</b></u>

change example-app-bash3 service requests to 100

result: example-app-bash3 is moved to the edge, then back and is moved to the cloud again

<h2> DSS SCENARIO #5:  </h2>
<H4>Test environment: "cloud-edge"</h4>
<H4>show how Apps are offloaded to edge/cloud nodes based on ECODA algorithm AND also resources allocated change as in "resources" optimisation</H4>

<u><b>initial configuration</b></u> 

The following Apps are configured with 'resources_latency' optimisation, then started and running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4

see that
example-app-bash1 and example-app-bash2 are instantiated on the edge
example-app-bash3 and example-app-bash4 are instantiated on the cloud

from ServiceReport, wait for startup time to be available
get the requests and put them in the [ECODA spreadsheet](ECODA.ods) and check the expected offloads, or check the "OptimisationReport" in the ConfServiceDSS UI

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the ECODA offloads are coherent with the spreadsheet or with the "OptimisationReport" in the ConfServiceDSS UI


<u><b>test #5.1</b></u>
wait some time without SLA violations

result#1: Apps' resources are scaled down by 10%

<u><b>test #5.2</b></u>

on example-app-bash2 simulate high CPU usage

```
APPNAME=example-app-bash2
kubectl --kubeconfig kind-kubeconfig2.yaml exec -it `kubectl --kubeconfig kind-kubeconfig2.yaml get po -n testsp2 | grep $APPNAME | awk '{ print $1}'` -n testsp2 -- sh
#simulate high CPU usage with 'stress'
stress --cpu 8
```

send a SLA violation about resources (sla_violation_sla_bash2.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_bash2.json
```

result#1: resource requests(300/300) go UP and the app offloaded to the cloud


result#2: on the other apps see resource requests going DOWN

get the requests, put them in the [ECODA spreadsheet](ECODA.ods) and check the expected offloads, or check the "OptimisationReport" in the ConfServiceDSS UI

result#3: the ECODA offloads are coherent with the spreadsheet or with the "OptimisationReport" in the ConfServiceDSS UI



<h2>  DSS SCENARIO #6: </h2> 
<H4>Test environment: "cloud-edge-faredge"</h4>
<H4>Goal: show the Apps offloading to cloud/edge/faredge nodes using the TTODA optimisation algorithm </H4>

<u><b>initial configuration</b></u> 

The following Apps are configured with "latency_faredge" optimisation, started and running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4
- example-app-bash5
- example-app-bash6


<u><b>test #6.1</b></u>

from ServiceReport, wait for startup time to be available
get the requests, put them in the [TTODA spreadsheet](TTODA.ods) and check the expected offloads, or check the "OptimisationReport" in the ConfServiceDSS UI

check the nodes where the pods are allocated

```
kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 -o wide
```

result: the TTODA offloads are coherent with the spreadsheet or with the "OptimisationReport" in the ConfServiceDSS UI

<h2>  DSS SCENARIO #7: </h2>
<H4>Test environment: "cloud-edge-faredge"</h4>
<H4>show how Apps are offloaded to faredge/edge/cloud nodes based on TTODA algorithm AND also resources allocated change as in "resources" optimisation</H4>

<u><b>initial configuration</b></u> 

The following Apps are configured with "resources_latency_faredge" optimisation, started and running:
- example-app-bash1
- example-app-bash2
- example-app-bash3
- example-app-bash4
- example-app-bash5
- example-app-bash6

<u><b>test #7.1</b></u>

on example-app-bash6 simulate high CPU usage

```
APPNAME=example-app-bash6
kubectl --kubeconfig kind-kubeconfig1.yaml exec -it `kubectl --kubeconfig kind-kubeconfig1.yaml get po -n testsp1 | grep $APPNAME | awk '{ print $1}'` -n testsp1 -- sh
#simulate high CPU usage with 'stress'
stress --cpu 8
```

send a SLA violation about resources (sla_violation_sla_bash6.json)

```
./send_dev_kafka.sh -t sla_violation -f sla_violation_sla_bash6.json
```

result#1: on example-app-bash6 App, resource requests(200/200) go UP
result#2: on the other apps, after some time, as SLA violations are not received, see resource requests going DOWN
(see "resources" optimisation)

get the requests, put them in the [TTODA spreadsheet](TTODA.ods) and check the expected offloads, or check the "OptimisationReport" in the ConfServiceDSS UI

result#3: the TTODA offloads are coherent with the spreadsheet or with the "OptimisationReport" in the ConfServiceDSS UI



