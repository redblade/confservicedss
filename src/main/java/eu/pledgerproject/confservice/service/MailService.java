package eu.pledgerproject.confservice.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import eu.pledgerproject.confservice.domain.Event;
import eu.pledgerproject.confservice.domain.User;
import eu.pledgerproject.confservice.repository.EventRepository;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private final JavaMailSender javaMailSender;
    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;
    private final EventRepository eventRepository;


    public MailService(JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine, EventRepository eventRepository) {

        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.eventRepository = eventRepository;
    }
    
    private void saveWarningEvent(String msg) {
    	if(log.isWarnEnabled()) {
			Event event = new Event();
			event.setTimestamp(Instant.now());
			event.setDetails(msg);
			event.setCategory("MailService");
			event.severity(Event.WARNING);
			eventRepository.save(event);
    	}
	}

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        String mailFrom = System.getenv("SPRING_MAIL_FROM");
        String username = System.getenv("SPRING_MAIL_USERNAME");
        String password = System.getenv("SPRING_MAIL_PASSWORD");
        String host = System.getenv("SPRING_MAIL_HOST");
        int port = Integer.parseInt(System.getenv("SPRING_MAIL_PORT"));

        ((JavaMailSenderImpl)javaMailSender).setUsername(username);
        ((JavaMailSenderImpl)javaMailSender).setPassword(password);
        ((JavaMailSenderImpl)javaMailSender).setHost(host);
        ((JavaMailSenderImpl)javaMailSender).setPort(port);
        
        log.info("MailService is using username " + ((JavaMailSenderImpl)javaMailSender).getUsername());

        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(mailFrom);
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        }  catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
            saveWarningEvent("Email could not be sent to user " + to + ". " + e.getMessage());
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, System.getenv().get("SPRING_MAIL_BASEURL"));//jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Async
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title");
    }
}
