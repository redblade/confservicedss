skupper.io tests

#INSTALL AND TEST FROM CLI/CTL
	 
 curl https://skupper.io/install.sh | sh

 #on remote cloud
 kubectl --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt  create ns demo
 
 /Users/francesco/bin/skupper init --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt --ingress nodeport --router-ingress-host  192.168.70.5 -n demo
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo status
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo token create secret4public.yaml  --token-type=cert

 #on local edge
 kubectl create ns private
 /Users/francesco/bin/skupper -n private init --ingress none
 /Users/francesco/bin/skupper -n private connect secret4public.yaml
 /Users/francesco/bin/skupper -n private link status
 
 #on remote cloud
 #create a deployment
 kubectl --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt  -n demo create deploy nginx --image=nginx

 #to expose via cli
 
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo expose --port 80 deployment nginx 

 #to expose via yaml
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo annotate --overwrite  deployment/nginx skupper.io/proxy="tcp"
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo annotate --overwrite  deployment/nginx skupper.io/port="80"
 
 #to unexpose via cli
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo unexpose  deployment nginx
 
 #to unexpose via yaml
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private annotate deployment/nginx skupper.io/proxy-
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private annotate deployment/nginx skupper.io/port- 
 
 
 #on public
 
 k run bash -n private --image=bash -- /bin/sh -c "sleep 1d"
 k get svc -n private #there is nginx
 k exec -it bash -n private -- bash
 rm -f index.html ; wget nginx
 
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt scale deploy nginx -n private --replicas=0
 rm -f index.html ; wget nginx
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt scale deploy nginx -n private --replicas=1
 
#DELETE
 /Users/francesco/bin/skupper -n private delete
 k delete ns private
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo delete
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt delete ns demo
 
#CONFIGURE
 #on remote infra
 /Users/francesco/bin/skupper init --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt --ingress nodeport --router-ingress-host  192.168.70.5 -n demo
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n demo status
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private token create secret4public.yaml  --token-type=cert

 #on local infra
 kubectl create ns private
 /Users/francesco/bin/skupper -n private init --ingress none
 /Users/francesco/bin/skupper -n private connect secret4public.yaml
 /Users/francesco/bin/skupper -n private link status
 #wait for status to be "active"

#TEST on ConfService
create an edge infrastructure (localhost)
create CatalogApp example-multi, then create App example-multi on SP francesco, so it creates two services: example-multi-private and example-multi-public
create ServiceConstraint to have example-multi-private on edge/localhost (priority 0) and example-multi-public on edge/localhost (priority 0) or cloud/ENG (priority 1)
start App example-multi
pods example-multi-private and example-multi-public are both on edge; example-multi-public has also a Service to allow example-multi-private to connect to example-multi-public 
do a "kubectl -i" on pod "example-multi-private" and do a "wget -O- example-multi-public" : it returns the hostname where pod example-multi-public is, which is initially the edge node
do a "migrate to worse option" on pod "example-multi-public": the pod and its service are moved to cloud infra
create an AppConstraint to link example-multi-private and example-multi-public services on port 80 and do "expose"
a service is created on edge pointing to cloud pod example-multi-public
repeat the wget test, it returns the hostname where example-multi-public is, which is now the cloud

basically: example-multi-private is transparently connected to example-multi-public, using a service named example-multi-public, regardless of where the pod is  


	 