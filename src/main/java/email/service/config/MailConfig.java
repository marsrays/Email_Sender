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
    private String apiKey;

    @Bean
    public SendGrid getSendGridSender() {
        return new SendGrid(apiKey);    // "SENDGRID_API_KEY"
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("smtp.gmail.com");
        mailSender.setHost("smtp.sendgrid.net");
        mailSender.setPort(587);

//        mailSender.setUsername("xxx.xxx@gmail.com");
//        mailSender.setPassword("password");
        mailSender.setUsername("apikey");   // "SENDGRID_USER_NAME" or "apikey"
        mailSender.setPassword(apiKey);     // "SENDGRID_PASSWORD" or "KEY"

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

}
