###HOWTO test ConfService/DSS with KinD in a cloud-edge infrastructure

This environment has

2 INFRASTRUCTUREs
- cluster1
- cluster2

6 NODEs 
- cluster1-control-plane (cloud)
- cluster1-worker        (cloud)
- cluster1-worker2       (edge )
- cluster1-worker3       (edge )
- cluster2-control-plane (cloud)
- cluster2-worker        (cloud)

Goldpinger[https://github.com/bloomberg/goldpinger] installed to measure the latency among nodes
metrics-server[https://github.com/kubernetes-sigs/metrics-server] installed to measure the resource allocation

The following instructions can be used to test Optimisations of type:
- "resource"
- "scaling"
- "offloading"
- "latency" (aka "ECODA" **src/main/java/eu/pledgerproject/confservice/monitoring/ECODA*.java**])
- "resource_latency" (which combines "resource" with "ECODA")

### Create the test environment
First, install [Kind tool](https://kind.sigs.k8s.io/docs/user/quick-start/)


1) delete Kind cluster

```
kind delete cluster --name cluster1
kind delete cluster --name cluster2
```
2) install Kind clusters (cluster1 and cluster2)

```
rm -fr kind-kubeconfig1.yaml
touch kind-kubeconfig1.yaml
kind create cluster --name cluster1 --kubeconfig kind-kubeconfig1.yaml --config kind-cluster1.yaml
kubectl --kubeconfig kind-kubeconfig1.yaml apply -f kind-metricserver.yaml
kubectl --kubeconfig kind-kubeconfig1.yaml apply -f kind-goldpinger.yaml
cp -r kind-kubeconfig1.yaml /var/tmp

rm -fr kind-kubeconfig2.yaml
touch kind-kubeconfig2.yaml
kind create cluster --name cluster2 --kubeconfig kind-kubeconfig2.yaml --config kind-cluster2.yaml
kubectl --kubeconfig kind-kubeconfig2.yaml apply -f kind-metricserver.yaml
kubectl --kubeconfig kind-kubeconfig2.yaml apply -f kind-goldpinger.yaml
cp -r kind-kubeconfig2.yaml /var/tmp

```

3) configure namespaces 

testSP1 can launch apps on cluster1 on namespace testsp1
testSP2 can launch apps on cluster1 and cluster2 on namespace testsp2

```
kubectl --kubeconfig kind-kubeconfig1.yaml create ns testsp1
kubectl --kubeconfig kind-kubeconfig2.yaml create ns testsp1
```
4) test node status and metrics-server status

```
kubectl --kubeconfig kind-kubeconfig1.yaml get no
kubectl --kubeconfig kind-kubeconfig2.yaml get no
kubectl --kubeconfig kind-kubeconfig1.yaml top no
kubectl --kubeconfig kind-kubeconfig2.yaml top no
```
5) expose goldpinger endpoints and check the status

```
kubectl --kubeconfig kind-kubeconfig1.yaml port-forward svc/goldpinger 30091:8080
kubectl --kubeconfig kind-kubeconfig2.yaml port-forward svc/goldpinger 30092:8080
```
open http://localhost:30091
open http://localhost:30092


###  Load the test configuration and optionally add latency to the network or change the applications startup times

6) load the MySQL dump

```
mysql -h localhost -D confservice -u root -proot -e "drop table db_lock"
java -cp target/confservicedss-2.4.4.jar -Dloader.main=eu.pledgerproject.confservice.InitDB org.springframework.boot.loader.PropertiesLauncher src/main/resources/config/sql/dump_kind.sql localhost 3306 root root

```

The configuration file **"dump_kind.sql"** has:

PROJECTs
- user testSP1 with 1 project on cluster1 and 1 project on cluster2

INFRASTRUCTUREs
- cluster1 with 7600m cpu 7600m mem
- cluster2 with 4000m cpu 4000m mem

NODEs 
- cluster1.control-plane (cloud) with 1000m cpu 1000m mem - unavailable for Apps
- cluster1.worker        (cloud) with 6000m cpu 6000m mem
- cluster1.worker2       (edge ) with  300m cpu  300m mem
- cluster1.worker3       (edge ) with  300m cpu  300m mem
- cluster2.control-plane (cloud) with 1000m cpu 1000m mem - unavailable for Apps
- cluster2.worker        (cloud) with 3000m cpu 3000m mem

APPs
- app1 with cpu/mem request 250/250 initial startup  5 
- app2 with cpu/mem request 300/300 initial startup 10
- app3 with cpu/mem request 300/300 initial startup 15
- app4 with cpu/mem request 200/200 initial startup 20


option#1 add latency on cloud worker node, then check the values on GoldPinger (response-time measured is 2xlatency)

cloud node (20ms)

```
docker exec -it `docker ps --format '{{.Names}}' | grep cluster1-worker | grep -v worker2 | grep -v worker3` bash
tc qdisc add dev eth0 root netem delay 20ms
```

remove latency

```
tc qdisc del dev eth0 root
```

option#2 change startup time

this adds 60s to normal startup time

```
readinessProbe:
  exec:
    command:
    - ls
  initialDelaySeconds: 55
  periodSeconds: 5
```

### Test the DSS optimisations
The optimisation scenarios are described in the **"doc/optimisations"** folder

