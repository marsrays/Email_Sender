package email.service.config;

import java.util.Properties;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import com.sendgrid.SendGrid;

@Configuration
public class MailConfig {

    @Value("${sendGridApiKey}")
    private String sendGridApiKey;
    private final String MAIL_GUN_SMTP_PW = "";

    @Bean
    public SendGrid getSendGridSender() {
        return new SendGrid(sendGridApiKey);    // "SENDGRID_API_KEY"
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Send By Gmail SMTP
//        mailSender.setHost("smtp.gmail.com");
//        mailSender.setUsername("xxx.xxx@gmail.com");
//        mailSender.setPassword("password");

        // Send By SendGrid SMTP
//        mailSender.setHost("smtp.sendgrid.net");
//        mailSender.setUsername("apikey");           // "SENDGRID_USER_NAME" or "apikey"
//        mailSender.setPassword(sendGridApiKey);     // "SENDGRID_PASSWORD" or "KEY"

        // Send By MailGun SMTP
        mailSender.setHost("smtp.mailgun.org");
        mailSender.setUsername("postmaster@XXX");   // "postmaster@MAILGUN_DOMAIN_NAME"
        mailSender.setPassword(MAIL_GUN_SMTP_PW);      // "MAILGUN_SMTP_PASSWORD"
        mailSender.setPort(587);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

}
