package eu.pledgerproject.confservice.monitoring;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.Infrastructure;
import eu.pledgerproject.confservice.domain.Node;
import eu.pledgerproject.confservice.domain.NodeReport;
import eu.pledgerproject.confservice.repository.EventRepository;
import eu.pledgerproject.confservice.repository.NodeReportRepository;
import eu.pledgerproject.confservice.repository.NodeRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/*
requires a monitoring_plugin configured in Infrastructure as 

{ 
  
  'goldpinger_endpoint': 'http://192.168.111.52:30080' (ENG)
  
}	   

*/


@Component
public class GoldPingerReader {
	public static final String HEADER = "goldpinger";
	public static final int TIMEOUT_SEC = 20; 
	
	private final org.slf4j.Logger log = LoggerFactory.getLogger(GoldPingerReader.class);
	
	private final NodeRepository nodeRepository;
	private final NodeReportRepository nodeReportRepository;
	private final EventRepository eventRepository;

	public GoldPingerReader(NodeRepository nodeRepository, NodeReportRepository nodeReportRepository, EventRepository eventRepository) {
		this.nodeRepository = nodeRepository;
		this.nodeReportRepository = nodeReportRepository;
		this.eventRepository = eventRepository;
	}
	
	private String getDataFromURL(String remoteUrl) throws IOException{
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
		  .url(remoteUrl)
		  .method("GET", null)
		  .build();
		Response response = client.newCall(request).execute(); 
		return response.body().string();
		
	}
	
	private void saveErrorEvent(String msg) {
		Event event = new Event();
		event.setTimestamp(Instant.now());
		event.setDetails(msg);
		event.setCategory("GoldPingerReader");
		event.severity(Event.ERROR);
		eventRepository.save(event);
	}
	
	public void storeMetrics(Infrastructure infrastructure, String goldPingerEndpoint, Instant timestamp) {
		try {
			log.info("GoldPingerReader storeMetrics...");
			String goldPingerData = getDataFromURL(goldPingerEndpoint + "/check_all?timeout=30");	
			JSONObject goldPingerJSON = new JSONObject(goldPingerData);
			
			while(goldPingerJSON.get("responses") == null) {
				log.info("GoldPingerReader - reading metrics again in " + TIMEOUT_SEC + " seconds");
				try{Thread.sleep(1000 * TIMEOUT_SEC);}catch(Exception e) {}
				
				goldPingerData = getDataFromURL(goldPingerData);	
				goldPingerJSON = new JSONObject(goldPingerData);
			}
			
			JSONObject responses = goldPingerJSON.getJSONObject("responses");
	
			Iterator<String> responsesKeys = responses.keys();
			while(responsesKeys.hasNext()) {
				String responsePodName =  responsesKeys.next();
				JSONObject responsePod = responses.getJSONObject(responsePodName);
				String responseHostIP = responsePod.getString("HostIP");
				
				Optional<Node> nodeDestination = nodeRepository.findByIpaddress("%"+responseHostIP+"%");
				if(nodeDestination.isPresent()) {
					JSONObject responsePodResults = responses.getJSONObject(responsePodName).getJSONObject("response").getJSONObject("podResults");					
					Iterator<String> responsePodResultsKeys = responsePodResults.keys();
					while(responsePodResultsKeys.hasNext()) {
						String responsePodResultsKey =  responsePodResultsKeys.next();

						JSONObject responsePodResult = responsePodResults.getJSONObject(responsePodResultsKey);
						String nodeIP = responsePodResult.getString("HostIP");
						if(nodeIP != null && !nodeIP.equals(responseHostIP)) {
						
							Optional<Node> nodeSource = nodeRepository.findByIpaddress("%"+nodeIP+"%");
							if(nodeSource.isPresent()) {
								Integer responseTimeMs = null;
								try {
									responseTimeMs = responsePodResult.getInt("response-time-ms");
								}catch(Exception e) {}
								
								if(responseTimeMs != null) {
									double latencyMs = (int)(1+responseTimeMs/2);
									Optional<NodeReport> nodeReportDB = nodeReportRepository.getNodeReportByNodeSourceIDNodeDestinationIDCategory(nodeSource.get().getId(), nodeDestination.get().getId(), "latency-ms");
									if(nodeReportDB.isPresent()) {
										nodeReportDB.get().setValue(latencyMs);
										nodeReportRepository.save(nodeReportDB.get());
									}
									else {
										NodeReport nodeReport = new NodeReport();
										nodeReport.setTimestamp(timestamp);
										nodeReport.setCategory(HEADER);
										nodeReport.setKey("latency-ms");
										nodeReport.setNode(nodeSource.get());
										nodeReport.setNodeDestination(nodeDestination.get());
										nodeReport.setValue(latencyMs);
										nodeReportRepository.save(nodeReport);
									}
								}
							}
						}
					}
				}
			}
		}catch(Exception e) {
			log.error("GoldPingerReader", e);
			saveErrorEvent("GoldPingerReader error " + e.getClass() + " " + e.getMessage());
		}

	}
	
	public int getAverageLatencyAmongTwoNodeGroups(Node nodeSrc, Set<Node> nodeListDst, Instant timestamp) {
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(nodeSrc);
		return getAverageLatencyAmongTwoNodeGroups(nodes, nodeListDst, timestamp);
	}
	
	public int getAverageLatencyAmongTwoNodeGroups(Set<Node> nodeListSrc, Set<Node> nodeListDst, Instant timestamp) {
		List<NodeReport> nodeReportList = nodeReportRepository.getAverageLatencyAmongTwoNodeGroups(nodeListSrc, nodeListDst, timestamp);
		
		int value = 0;
		for(NodeReport nodeReport : nodeReportList) {
			value += nodeReport.getValue();
		}
		if(nodeReportList.size() > 0) {
			return (int) value / nodeReportList.size();
		}
		else {
			return 0;
		}
	}
	
}
