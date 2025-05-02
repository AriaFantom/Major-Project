package com.project.bizconnect;

import com.project.bizconnect.entity.Role;
import com.project.bizconnect.entity.User;
import com.project.bizconnect.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BizconnectApplication implements CommandLineRunner {

	@Autowired
	private UsersRepository usersRepository;

	public static void main(String[] args) {
		SpringApplication.run(BizconnectApplication.class, args);
	}

	public void run(String... args) {
		User adminAccount = usersRepository.findByRole(Role.ADMIN);
		if(null == adminAccount) {
			User user = new User();
			user.setEmail("admin@mail.com");
			user.setFirstName("admin");
			user.setLastName("admin");
			user.setRole(Role.ADMIN);
			user.setPassword(new BCryptPasswordEncoder().encode("1234"));

			usersRepository.save(user);

		}
	}

}
