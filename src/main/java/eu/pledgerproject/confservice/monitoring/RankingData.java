package eu.pledgerproject.confservice.monitoring;

import java.util.HashSet;
import java.util.Set;

import eu.pledgerproject.confservice.domain.Node;

public class RankingData {

	private int ranking;
	private Set<Node> nodeSet;
	
	public RankingData(int ranking, Set<Node> nodeSet){
		this.ranking = ranking;
		this.nodeSet = new HashSet<Node>(nodeSet);
	}
	
	public int getRanking() {
		return ranking;
	}
	public Set<Node> getNodeSet(){
		return this.nodeSet;
	}
	
}
