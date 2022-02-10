skupper.io tests


	 
 curl https://skupper.io/install.sh | sh

 #on private
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt  create ns private
 k --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt  -n private create deploy nginx --image=nginx
 
 /Users/francesco/bin/skupper init --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt --ingress nodeport --router-ingress-host  192.168.70.5 -n private
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private status

 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private expose --port 80 deployment nginx 
 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private status

 /Users/francesco/bin/skupper --kubeconfig /Users/francesco/script/eng_dhub_kube_config.txt -n private token create secret4public.yaml  --token-type=cert

 #on public
 k create ns private
 /Users/francesco/bin/skupper -n private init
 /Users/francesco/bin/skupper -n private connect secret4public.yaml
 /Users/francesco/bin/skupper -n private link status
 
 k run bash -n private --image=bash -- /bin/sh -c "sleep 1d"
 k get svc -n private #there is nginx
 k exec -it bash -n private -- bash
 wget nginx
 
	 