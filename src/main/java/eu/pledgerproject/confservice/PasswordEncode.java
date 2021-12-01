package eu.pledgerproject.confservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncode {

	public static void main(String[] args) {
		String password = args[0];
		System.out.println("Encoded password for " + password + " is " + (new BCryptPasswordEncoder().encode(password)));
	}

}
