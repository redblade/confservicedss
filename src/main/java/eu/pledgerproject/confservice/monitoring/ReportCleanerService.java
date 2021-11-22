package eu.pledgerproject.confservice.monitoring;

import java.time.Instant;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.repository.BenchmarkReportRepository;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.InfrastructureReportRepository;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.repository.ServiceReportRepository;

@Component
public class ReportCleanerService {
	public static final int REPORT_CLEAN_HOUR = 24;
	
	private final Logger log = LoggerFactory.getLogger(ReportCleanerService.class);
	private final EventRepository eventRepository;
	private final ServiceReportRepository serviceReportRepository;
	private final InfrastructureReportRepository infrastructureReportRepository;
	private final NodeReportRepository nodeReportRepository;
	private final BenchmarkReportRepository benchmarkReportRepository;
	
	public ReportCleanerService(EventRepository eventRepository, ServiceReportRepository serviceReportRepository, InfrastructureReportRepository infrastructureReportRepository, NodeReportRepository nodeReportRepository, BenchmarkReportRepository benchmarkReportRepository) {
		this.eventRepository = eventRepository;
		this.serviceReportRepository = serviceReportRepository;
		this.infrastructureReportRepository = infrastructureReportRepository;
		this.nodeReportRepository = nodeReportRepository;
		this.benchmarkReportRepository = benchmarkReportRepository;
	}
	
	@Scheduled(cron = "0 0 */1 * * *")
	@Transactional
	public void executeTask() {
		if(!ControlFlag.READ_ONLY_MODE_ENABLED){
	
			log.info("LogCleaner removed events older than " + REPORT_CLEAN_HOUR + " h");
			
			Event event = new Event();
			event.setCategory("ReportCleaner");
			event.setDetails("started");
			eventRepository.save(event);
			
			Instant timestampClean = Instant.now().minusSeconds(REPORT_CLEAN_HOUR*60*60);
			serviceReportRepository.deleteOld(timestampClean);
			infrastructureReportRepository.deleteOld(timestampClean);
			nodeReportRepository.deleteOld(timestampClean);
			benchmarkReportRepository.deleteOld(timestampClean);
		}
	}

}
