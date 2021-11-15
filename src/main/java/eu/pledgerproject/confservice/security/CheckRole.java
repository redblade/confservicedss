package eu.pledgerproject.confservice.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class CheckRole {

	public static void block(String role) {
		SecurityContext securityContext = SecurityContextHolder.getContext();
        if(securityContext.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority(role))) {
        	throw new RuntimeException("Unauthorized");
        }
	}
	
}
