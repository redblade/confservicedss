package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.optimisation.QuotaMonitoringReader;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.InfrastructureRepository;

@Component
public class MonitoringService {
    private final Logger log = LoggerFactory.getLogger(MonitoringService.class);

	private final InfrastructureRepository infrastructureRepository;
	private final MetricsServerReader metricsServerReader;
	private final PrometheusReaderKubernetes prometheusReaderKubernetes;
	private final GoldPingerReader goldPingerReader;
	private final EventRepository eventRepository;
	private final NodeAutodiscovery nodeAutodiscovery;
	private final QuotaMonitoringReader quotaMonitoringReader;

	public static String CPU = "cpu";
	public static String CPU_LABEL = "cpu_millicore";
	public static String MEMORY = "memory";
	public static String MEMORY_LABEL = "memory_mb";
	
	public static final String INITIAL_MEMORY_MB = "initial_memory_mb";
	public static final String INITIAL_CPU_MILLICORE = "initial_cpu_millicore";
	public static final String MIN_MEMORY_MB = "min_memory_mb";
	public static final String MIN_CPU_MILLICORE = "min_cpu_millicore";
	
	
	public MonitoringService(InfrastructureRepository infrastructureRepository, MetricsServerReader metricsServerReader, PrometheusReaderKubernetes prometheusReaderKubernetes, GoldPingerReader goldPingerReader, EventRepository eventRepository, NodeAutodiscovery nodeAutodiscovery, QuotaMonitoringReader quotaMonitoringReader) {
		this.infrastructureRepository = infrastructureRepository;
		this.metricsServerReader = metricsServerReader;
		this.prometheusReaderKubernetes = prometheusReaderKubernetes;
		this.goldPingerReader = goldPingerReader;
		this.eventRepository = eventRepository;
		this.nodeAutodiscovery = nodeAutodiscovery;
		this.quotaMonitoringReader = quotaMonitoringReader;
	}
	
	private void saveErrorEvent(String msg) {
    	if(log.isErrorEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("MonitoringService");
			event.severity(Event.ERROR);
			eventRepository.save(event);
    	}
	}

	@Scheduled(cron = "0 */1 * * * *")
	public void executeTask() {
		if(!ControlFlags.READ_ONLY_MODE_ENABLED){

			log.info("MonitoringService started");
			
			Event event = new Event();
			event.setCategory("MonitoringService");
			event.setDetails("monitoring started");
			eventRepository.save(event);
			
			for (Infrastructure infrastructure : infrastructureRepository.findAllWithNodes()) {
				Instant timestamp = Instant.now();
				
				log.info("MonitoringService is working on infrastructure " + infrastructure.getName());
				try {
					log.info("MonitoringService: " + infrastructure.getName());
					
					if(infrastructure.getNodeSets().size() == 0) {
						nodeAutodiscovery.autodiscoveryNodes(infrastructure);
					}
	
					String infrastructureType = infrastructure.getType();
					if(infrastructureType != null && infrastructureType.equals("K8S")) {
						
						Map<String, String> monitoringProperties = ConverterJSON.convertToMap(infrastructure.getMonitoringPlugin());
							
						if(MetricsServerReader.HEADER.equals(monitoringProperties.get("monitoring_type"))) {
							metricsServerReader.storeMetrics(infrastructure, timestamp);
						}
						else if(PrometheusReaderKubernetes.HEADER.equals(monitoringProperties.get("monitoring_type"))) {
							String endpoint = monitoringProperties.get("prometheus_endpoint");
							prometheusReaderKubernetes.storeMetrics(infrastructure, endpoint, timestamp);
						}
						
						if(monitoringProperties.containsKey("goldpinger_endpoint")) {
							String endpoint = monitoringProperties.get("goldpinger_endpoint");
							goldPingerReader.storeMetrics(infrastructure, endpoint, timestamp);
						}
					}
					
					quotaMonitoringReader.storeMetrics(timestamp);
					
				} catch(Exception e) {
					log.error("MonitoringService", e);
					saveErrorEvent("MonitoringService error " + e.getClass() + " " + e.getMessage());
				}
			}
		}
	}
}
