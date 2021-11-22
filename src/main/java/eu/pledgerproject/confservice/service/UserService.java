package eu.pledgerproject.confservice.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.pledgerproject.confservice.config.Constants;
import eu.pledgerproject.confservice.domain.Authority;
import eu.pledgerproject.confservice.domain.InfrastructureProvider;
import eu.pledgerproject.confservice.domain.ServiceProvider;
import eu.pledgerproject.confservice.domain.User;
import eu.pledgerproject.confservice.monitoring.ControlFlag;
import eu.pledgerproject.confservice.repository.AuthorityRepository;
import eu.pledgerproject.confservice.repository.InfrastructureProviderRepository;
import eu.pledgerproject.confservice.repository.ServiceProviderRepository;
import eu.pledgerproject.confservice.repository.UserRepository;
import eu.pledgerproject.confservice.security.SecurityUtils;
import eu.pledgerproject.confservice.service.dto.UserDTO;
import io.github.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {
	private static final String DEFAULT_SERVICE_PROVIDER_PREFERENCES = "'{\n  \"monitoring.steadyServices.maxResourceUsedPercentage\": 70,\n  \"monitoring.criticalServices.maxResourceBufferPercentage\": 20,\n  \"monitoring.slaViolation.periodSec\": 300,\n  \"autoscale.percentage\": 10\n}')";
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final InfrastructureProviderRepository infrastructureProviderRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, ServiceProviderRepository serviceProviderRepository, InfrastructureProviderRepository infrastructureProviderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.infrastructureProviderRepository = infrastructureProviderRepository;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        return userRepository.findOneByResetKey(key)
            .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
            .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
            });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                return user;
            });
    }

    public User registerUser(UserDTO userDTO, String password) {
        userRepository.findOneByLogin(userDTO.getLogin().toLowerCase()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new UsernameAlreadyUsedException();
            }
        });
        userRepository.findOneByEmailIgnoreCase(userDTO.getEmail()).ifPresent(existingUser -> {
            boolean removed = removeNonActivatedUser(existingUser);
            if (!removed) {
                throw new EmailAlreadyUsedException();
            }
        });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.getActivated()) {
             return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        return true;
    }
    
    private void checkLeastPriv(Set<Authority> authorities) {
        Authority apiAuthority = new Authority("ROLE_ROAPI");
        Authority adminAuthority = new Authority("ROLE_ADMIN");
        
        if(authorities.size() > 1 && authorities.contains(adminAuthority)) {
        	throw new RuntimeException("too many roles");
        }
        if(authorities.size() > 1 && authorities.contains(apiAuthority)) {
        	throw new RuntimeException("too many roles");
        }

    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        
        Authority spAuthority = new Authority("ROLE_SP");
        Authority ipAuthority = new Authority("ROLE_IP");
        
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO.getAuthorities().stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            checkLeastPriv(authorities);
            user.setAuthorities(authorities);
        }
        

        if(user.getAuthorities() != null && user.getAuthorities().contains(spAuthority)) {
        	ServiceProvider serviceProvider = new ServiceProvider();
        	serviceProvider.setName(userDTO.getFirstName());
        	serviceProvider.setOrganisation(userDTO.getLastName());
        	serviceProvider.setPreferences(DEFAULT_SERVICE_PROVIDER_PREFERENCES);
        	serviceProviderRepository.save(serviceProvider);
        }
        else if(user.getAuthorities() != null && user.getAuthorities().contains(ipAuthority)) {
        	InfrastructureProvider infrastructureProvider = new InfrastructureProvider();
        	infrastructureProvider.setName(userDTO.getFirstName());
        	infrastructureProvider.setOrganisation(userDTO.getLastName());
        	infrastructureProviderRepository.save(infrastructureProvider);
        }
        if(user.getActivated()) {
        	
        }
        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
    	
    	return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
            
            Authority spAuthority = new Authority(); spAuthority.setName("ROLE_SP");
            Authority ipAuthority = new Authority(); ipAuthority.setName("ROLE_IP");

            if(user.getAuthorities() != null && user.getAuthorities().contains(spAuthority) && user.getAuthorities().contains(ipAuthority)) {
            	Optional<ServiceProvider> optionalServiceProvider = serviceProviderRepository.findByName(user.getFirstName());
            	Optional<InfrastructureProvider> optionalInfrastructureProvider = infrastructureProviderRepository.findByName(user.getFirstName());
            	if(optionalServiceProvider.isPresent()) {
            		serviceProviderRepository.delete(optionalServiceProvider.get());
            	}
            	if(optionalInfrastructureProvider.isPresent()) {
            		infrastructureProviderRepository.delete(optionalInfrastructureProvider.get());
            	}
            }
            else if(user.getAuthorities() != null && user.getAuthorities().contains(spAuthority)) {
            	Optional<ServiceProvider> optionalServiceProvider = serviceProviderRepository.findByName(user.getFirstName());
            	if(optionalServiceProvider.isPresent()) {
            		serviceProviderRepository.delete(optionalServiceProvider.get());
            	}
            }
            else if(user.getAuthorities() != null && user.getAuthorities().contains(ipAuthority)) {
            	Optional<InfrastructureProvider> optionalInfrastructureProvider = infrastructureProviderRepository.findByName(user.getFirstName());
            	if(optionalInfrastructureProvider.isPresent()) {
            		infrastructureProviderRepository.delete(optionalInfrastructureProvider.get());
            	}
            }

        });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                log.debug("Changed Information for User: {}", user);
            });
    }


    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                log.debug("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
		if(!ControlFlag.READ_ONLY_MODE_ENABLED){

	        userRepository
	            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
	            .forEach(user -> {
	                log.debug("Deleting not activated user {}", user.getLogin());
	                userRepository.delete(user);
	            });
		}
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

}
