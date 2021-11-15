package eu.pledgerproject.confservice.web.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.pledgerproject.confservice.service.NetworkService;

/**
 * REST controller for managing {@link eu.pledgerproject.confservice.domain.Infrastructure}.
 */
@RestController
@RequestMapping("/api")
public class ToolsResource {

    private final Logger log = LoggerFactory.getLogger(ToolsResource.class);

    private NetworkService networkService;
    public ToolsResource(NetworkService networkService) {
    	this.networkService = networkService;
    }

	@GetMapping("/res/grafana")
	public void grafana(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
	    httpServletResponse.setHeader("Location", "http://"+getIP(httpServletRequest)+":31255/d/S37dakeGz/dss-resources?orgId=1t");
	    httpServletResponse.setStatus(302);
	}
	@GetMapping("/res/flowui")
	public void flowui(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
	    httpServletResponse.setHeader("Location", "http://"+getIP(httpServletRequest)+":31858/");
	    httpServletResponse.setStatus(302);
	}
	@GetMapping("/res/jsonui")
	public void jsonui(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		httpServletResponse.setHeader("Location", "http://"+getIP(httpServletRequest)+":31548/");
	    httpServletResponse.setStatus(302);
	}
	@GetMapping("/res/goldpinger")
	public void goldpinger(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
	    httpServletResponse.setHeader("Location", "http://"+getIP(httpServletRequest)+":30080/");
	    httpServletResponse.setStatus(302);
	}
	
    
    @GetMapping("/res/networkJSON")
    public ResponseEntity<Object> getAllInfrastructuresJSON() throws IOException{
        log.debug("REST request to get a page of Infrastructures in JSON format");
        
        String result = networkService.getAllInfrastructuresJSON();
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }
    
    private String getIP(HttpServletRequest httpServletRequest) {
    	return httpServletRequest.getLocalAddr();
    }

}
