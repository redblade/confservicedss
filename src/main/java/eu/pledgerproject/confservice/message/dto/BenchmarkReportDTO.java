package eu.pledgerproject.confservice.message.dto;

public class BenchmarkReportDTO {
	
	public String metric;
	public String tool;
	public String workload;
	public String category;
	public Long pledgerInfrastructure;
	public Long pledgerNode;
	public Long pledgerServiceProvider;
	public Long pledgerProject;

    public Double mean;
	public Integer interval;
	public Double stabilityIndex;
	
	public BenchmarkReportDTO() {}

	public BenchmarkReportDTO(String metric, String tool, String workload, String category, Long pledgerInfrastructure, Long pledgerNode,
			Long pledgerServiceProvider, Long pledgerProject, Double mean, Integer interval, Double stabilityIndex) {
		super();
		this.metric = metric;
		this.tool = tool;
		this.workload = workload;
		this.category = category;
		this.pledgerInfrastructure = pledgerInfrastructure;
		this.pledgerNode = pledgerNode;
		this.pledgerServiceProvider = pledgerServiceProvider;
		this.pledgerProject = pledgerProject;
		this.mean = mean;
		this.interval = interval;
		this.stabilityIndex = stabilityIndex;
	}

	@Override
	public String toString() {
		return "BenchmarkReportDTO [metric=" + metric + ", tool=" + tool + ", workload=" + workload + ", category=" + category
				+ ", pledgerInfrastructure=" + pledgerInfrastructure + ", pledgerNode=" + pledgerNode + ", pledgerServiceProvider="
				+ pledgerServiceProvider + ", pledgerProject=" + pledgerProject + ", mean=" + mean + ", interval="
				+ interval + ", stabilityIndex=" + stabilityIndex + "]";
	}




	
}

//{"benchmarkId":"1","benchmarkName":"mybench","elapsedTimeSec":"123","stabilityIndex":"1"}
