To test Benchmarking, load the test dump sql.
Then use Postman to login with api user, then invoke:

scenario 1) Benchmark chosen via AppProfiler:      service id 9,  with profile 'sysbench-cpu:cpu-10k:0'
http://localhost:8080/api/nodes/integration/9/5
 
scenario 2) Benchmark chosen via service category: service id 10, with profile 'cpu-intensive'
http://localhost:8080/api/nodes/integration/10/5

scenario 3) Benchmark chosen via node capacity:    service id 11
http://localhost:8080/api/nodes/integration/11/5

