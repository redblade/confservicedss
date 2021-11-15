package eu.pledgerproject.confservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncode {

	public static void main(String[] args) {
		System.out.println(new BCryptPasswordEncoder().encode(args[0]));
	}

}
