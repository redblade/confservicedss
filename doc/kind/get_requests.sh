#works on Mac OSx
#example ./get_requests.sh  kind-kubeconfig1.yaml testsp1 sample-app-bash1

export kubeconfig=$1
export ns=$2
export app=$3

echo app $app on ns $ns

export pod_name=`kubectl --kubeconfig $kubeconfig get po -n $ns | grep $app | awk '{print $1}'`
export request_cpu=`kubectl --kubeconfig $kubeconfig  get po $pod_name -n $ns -o jsonpath='{.spec.containers[*].resources.limits.cpu}'`
export request_mem=`kubectl --kubeconfig $kubeconfig  get po $pod_name -n $ns -o jsonpath='{.spec.containers[*].resources.limits.memory}'`

echo request cpu/mem $request_cpu/$request_mem

