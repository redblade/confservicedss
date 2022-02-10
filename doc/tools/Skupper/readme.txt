skupper.io tests


	 
 curl https://skupper.io/install.sh | sh

 #on private
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt  create ns private
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt  -n private create deploy nginx --image=nginx
 
 /Users/francesco/bin/skupper init --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt --ingress nodeport --router-ingress-host  192.168.70.5 -n private
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private status

 #to expose via cli
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private expose --port 80 deployment nginx 

 #to expose via yaml
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private annotate --overwrite  deployment/nginx skupper.io/proxy="tcp"
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private annotate --overwrite  deployment/nginx skupper.io/port="80"
 
 #to unexpose via cli
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private unexpose  deployment nginx
 
 #to unexpose via yaml
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private annotate deployment/nginx skupper.io/proxy-
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private annotate deployment/nginx skupper.io/port- 
 
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private status

 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private token create secret4public.yaml  --token-type=cert

 #on public
 k create ns private
 /Users/francesco/bin/skupper -n private init --ingress none
 /Users/francesco/bin/skupper -n private connect secret4public.yaml
 /Users/francesco/bin/skupper -n private link status
 
 k run bash -n private --image=bash -- /bin/sh -c "sleep 1d"
 k get svc -n private #there is nginx
 k exec -it bash -n private -- bash
 rm -f index.html ; wget nginx
 
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt scale deploy nginx -n private --replicas=0
 rm -f index.html ; wget nginx
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt scale deploy nginx -n private --replicas=1
 

	 