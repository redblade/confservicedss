package eu.pledgerproject.confservice.monitoring;

public class ControlFlags {
	
	static {
		READ_ONLY_MODE_ENABLED = "TRUE".equals(System.getenv("READ_ONLY_MODE_ENABLED"));
		BENCHMARK_DSS_DISABLED = "TRUE".equals(System.getenv("BENCHMARK_DSS_DISABLED"));
		DOCKER_ENABLED         = "TRUE".equals(System.getenv("DOCKER_ENABLED"));
		MULTICLOUD_ENABLED     = "TRUE".equals(System.getenv("MULTICLOUD_ENABLED"));
		SLAMANAGER_ENABLED     = "TRUE".equals(System.getenv("SLAMANAGER_ENABLED"));
	}
	
	
	//these flags are used for testing and should be all FALSE
	public static final boolean READ_ONLY_MODE_ENABLED;
	public static final boolean BENCHMARK_DSS_DISABLED;
	public static final boolean DOCKER_ENABLED;
	public static final boolean MULTICLOUD_ENABLED;
	public static final boolean SLAMANAGER_ENABLED;
}
