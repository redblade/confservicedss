package eu.pledgerproject.confservice.optimisation;

public class Constants {
	
	public static final String DEFAULT_AUTOSCALE_PERCENTAGE = "10";
	public static final int GRACE_PERIOD_SERVICE_START_FOR_SLA_SEC = 30;

	
	public static final int DEFAULT_INITIAL_MEMORY_MB = 200;
	public static final int DEFAULT_INITIAL_CPU_MILLICORE = 200;
	public static final int DEFAULT_MIN_MEMORY_MB = 100;
	public static final int DEFAULT_MIN_CPU_MILLICORE = 100;
	public static final int DEFAULT_MAX_MEMORY_MB = 300;
	public static final int DEFAULT_MAX_CPU_MILLICORE = 300;

	
	public static final String INITIAL_MEMORY_MB = "initial_memory_mb";
	public static final String INITIAL_CPU_MILLICORE = "initial_cpu_millicore";
	public static final String MIN_MEMORY_MB = "min_memory_mb";
	public static final String MIN_CPU_MILLICORE = "min_cpu_millicore";
	public static final String MAX_MEMORY_MB = "max_memory_mb";
	public static final String MAX_CPU_MILLICORE = "max_cpu_millicore";
	
    public static final int DEFAULT_REPLICAS = 1;
    public static final int DEFAULT_MAX_REPLICAS = 3;
    
    public static final String REPLICAS = "replicas";
    public static final String MAX_REPLICAS = "max_replicas";
	
	
    public static final String SCALING_HORIZONTAL = "horizontal";
    public static final String SCALING_VERTICAL = "vertical";
    public static final String SCALING = "scaling";


}
