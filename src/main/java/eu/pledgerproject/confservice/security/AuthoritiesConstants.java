package eu.pledgerproject.confservice.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";
    
    public static final String USER = "ROLE_USER";

    public static final String SP = "ROLE_SP";
    
    public static final String IP = "ROLE_IP";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    private AuthoritiesConstants() {
    }
}
