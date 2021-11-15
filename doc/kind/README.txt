HOWTO test ConfService/DSS with KinD (Kubernetes in Docker)

#0) 
install Kind tool https://kind.sigs.k8s.io/docs/user/quick-start/ 
load the MySQL dump src/main/resources/config/sql/dump_kind.sql 

#1) delete Kind cluster
kind delete cluster --name cluster1
kind delete cluster --name cluster2

#2) install Kind clusters, cluster1 (1 master cloud + 1 worker cloud + 2 workers edge), cluster2 (1 master edge + 1 worker edge)
rm -fr kind-kubeconfig1.yaml
touch kind-kubeconfig1.yaml
kind create cluster --name cluster1 --kubeconfig kind-kubeconfig1.yaml --config kind-cluster1.yaml
kubectl --kubeconfig kind-kubeconfig1.yaml apply -f kind-metricserver.yaml
kubectl --kubeconfig kind-kubeconfig1.yaml apply -f kind-goldpinger.yaml

rm -fr kind-kubeconfig2.yaml
touch kind-kubeconfig2.yaml
kind create cluster --name cluster2 --kubeconfig kind-kubeconfig2.yaml --config kind-cluster2.yaml
kubectl --kubeconfig kind-kubeconfig2.yaml apply -f kind-metricserver.yaml
kubectl --kubeconfig kind-kubeconfig2.yaml apply -f kind-goldpinger.yaml

#3)
#configure namespaces 
#testSP1 can launch apps on cluster1 on namespace testsp1
#testSP2 can launch apps on cluster1 and cluster2 on namespace testsp2
kubectl --kubeconfig kind-kubeconfig1.yaml create ns testsp1
kubectl --kubeconfig kind-kubeconfig1.yaml create ns testsp2
kubectl --kubeconfig kind-kubeconfig2.yaml create ns testsp2

#4)
#label nodes just for convenience
kubectl --kubeconfig kind-kubeconfig1.yaml label node cluster1-control-plane location=cloud capacity_cpu=1000m capacity_mem=1000mi
kubectl --kubeconfig kind-kubeconfig1.yaml label node cluster1-worker        location=cloud capacity_cpu=6000m capacity_mem=6000mi
kubectl --kubeconfig kind-kubeconfig1.yaml label node cluster1-worker2       location=edge  capacity_cpu=300m  capacity_mem=300mi
kubectl --kubeconfig kind-kubeconfig1.yaml label node cluster1-worker3       location=edge  capacity_cpu=300m  capacity_mem=300mi
kubectl --kubeconfig kind-kubeconfig2.yaml label node cluster2-control-plane location=edge  capacity_cpu=1000m capacity_mem=1000mi
kubectl --kubeconfig kind-kubeconfig2.yaml label node cluster2-worker        location=edge  capacity_cpu=300m  capacity_mem=300mi

#5)
#test node status and metrics-server status
kubectl --kubeconfig kind-kubeconfig1.yaml get no
kubectl --kubeconfig kind-kubeconfig2.yaml get no
kubectl --kubeconfig kind-kubeconfig1.yaml top no
kubectl --kubeconfig kind-kubeconfig2.yaml top no

#6) 
#expose goldpinger endpoints and check the status
kubectl --kubeconfig kind-kubeconfig1.yaml port-forward svc/goldpinger 30091:8080
kubectl --kubeconfig kind-kubeconfig2.yaml port-forward svc/goldpinger 30092:8080
open http://localhost:30091
open http://localhost:30092

HOWTO do tests and see the DSS optimisation at work

The configuration file "dump_kind.sql" has:
user testSP1 with 1 project on cluster1 with quota 5000m cpu 5000m mem
user testSP2 with 1 project on cluster1 with quota 1000m cpu 1000m mem
user testSP2 with 1 project on cluster2 with quota 300m  cpu  300m mem

app1 with cpu/mem request 250/250 initial startup  5 
app2 with cpu/mem request 300/300 initial startup 10
app3 with cpu/mem request 300/300 initial startup 15
app4 with cpu/mem request 200/200 initial startup 20

TEST 1) add latency to the cluster1 cloud worker and check the values on GoldPinger (response-time measured is latency*2)
docker exec -it `docker ps --format '{{.Names}}' | grep cluster1-worker | grep -v worker2 | grep -v worker3` bash

#add latency 
tc qdisc add dev eth0 root netem delay 50ms

#remove latency
tc qdisc del dev eth0 root

TEST 2) change startup time

#this adds 60s to normal startup time
readinessProbe:
  exec:
    command:
    - ls
  initialDelaySeconds: 55
  periodSeconds: 5
  
