package eu.pledgerproject.confservice.simulator;

public class Simulator {

	public static void main(String[] args) {
		//serviceId, count, stepSec, cpuMean, memoryMean, -/+ percentage
		
		//risk-detector
		ResourceUsage.main(new String[] {"3", "10", "60", "40", "100", "10"});
		
		//basic-analytics
		ResourceUsage.main(new String[] {"4", "10", "60", "30", "80", "10"});

		//sla-violation
		SlaViolationSimulator.main(new String[] {"1", "Serious"});
	}

}
