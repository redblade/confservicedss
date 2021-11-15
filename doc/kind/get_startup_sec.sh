#works on Mac OSx
#example ./get_startup_sec.sh  kind-kubeconfig1.yaml testsp1 sample-app-bash1

export kubeconfig=$1
export ns=$2
export app=$3

echo app $app on ns $ns

export pod_name=`kubectl --kubeconfig $kubeconfig get po -n $ns | grep $app | awk '{print $1}'`
export date_scheduled=`kubectl --kubeconfig $kubeconfig  get po $pod_name -n $ns -o jsonpath='{.status.conditions[?(@.type=="PodScheduled")].lastTransitionTime}'`
export date_ready=`kubectl --kubeconfig $kubeconfig  get po $pod_name -n $ns -o jsonpath='{.status.conditions[?(@.type=="Ready")].lastTransitionTime}'`

export date_scheduled_formatted=`date -j -f "%Y-%m-%dT%H:%M:%SZ" $date_scheduled "+%s"`
export date_ready_formatted=`date -j -f "%Y-%m-%dT%H:%M:%SZ" $date_ready "+%s"`
export startuptime_sec=$(( $date_ready_formatted-$date_scheduled_formatted ))

echo sched at $date_scheduled
echo ready at $date_ready
echo startup took $startuptime_sec sec

