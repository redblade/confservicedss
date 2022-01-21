package eu.pledgerproject.confservice.optimisation;

class ServiceResourcePlan {
	int cpu;
	int mem;
	String msg;
	
	ServiceResourcePlan(int[] cpu_mem, String msg){
		this.cpu = cpu_mem[0];
		this.mem = cpu_mem[1];
		this.msg = msg;
	}
}