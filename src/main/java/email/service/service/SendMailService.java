package email.service.service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.HttpStatus;
import org.rapidoid.u.U;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

import email.service.enumeration.TemplateType;

@Service
public class SendMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMailService.class);

    /**
     * 收件人郵箱地址
     */
    private static final String TO = "ray.liu@nexiosoft.com";
    private static final String TO2 = "DEF@gmail.com";

    /**
     * 寄件人郵箱地址
     */
    private static final String FROM = "TEST<noreply@email.com>";

    @Value("${mailGunApiKey}")
    private String mailGunApiKey;
    @Value("${mailGunUserName}")
    private String mailGunUserName;

    @Autowired
    private SendGrid sendGrid;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private Handlebars handlebars;

    @Autowired
    private PebbleTemplate recalculateTemplate;

    @Autowired
    private PebbleTemplate activateTemplate;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Base64.Encoder encoder = Base64.getEncoder();

    /**
     * 利用 SendGrid 提供的 JAVA 框架寄信
     * @param templateType 信件模板
     */
    public void sendHTMLEmailBySendGrid(TemplateType templateType) {
        try {
            // 透過 Handlebars 或是 Jinja Template 生成信件內容
            Content content = new Content("text/html", getHtmlByHandlebars(templateType)); // getHtmlByTemplate(templateType));
            Mail mail = new Mail(new Email(FROM), templateType.getTitle(), new Email(TO), content);

            // SendGrid 可以一次寄發相同內容不同信(Personalization)的 Email
            // 各別群增加其他收信人，可以繼續呼叫 addTo( email_address ) 即可，另外可以 addCc
            mail.getPersonalization().get(0).addTo(new Email(TO2));

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            LOGGER.info("Sent message successfully.... {} , {}, {}", response.getStatusCode(), response.getBody(), response.getHeaders());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 利用 JavaMail 框架寄信
     * @param templateType 信件模板
     */
    public void sendHTMLEmailByJavaMail(TemplateType templateType) {
        try {
            // 使用預設建構子生成信件訊息
//            MimeMessage message = getMimeMessageByDefault();

            // 使用 MimeMessageHelper 產生信件基本資訊，再透過 Handlebars 或是 Jinja Template 生成信件內容
            MimeMessage message = getMimeMessageByHelper(templateType.getTitle(), getHtmlByHandlebars(templateType)); // getHtmlByTemplate(templateType));

            // 利用 Transport 發送 Email
//            Transport.send(message);

            // 利用 JavaMailSender 發送 Email
            emailSender.send(message);
            LOGGER.info("Sent message successfully....");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 利用 SendGrid 提供的 JAVA 框架寄信
     * @param templateType 信件模板
     */
    public void sendHTMLEmailByMailGun(TemplateType templateType) {
        try {
            MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
            String auth = "api:" + mailGunApiKey;
            headerMap.add("Authorization", "Basic " + encoder.encodeToString(auth.getBytes(StandardCharsets.UTF_8)));
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("from", FROM);
            map.add("to", TO);
            map.add("subject", templateType.getTitle());
            map.add("html", getHtmlByHandlebars(templateType)
            );
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headerMap);

            ResponseEntity<String> response = restTemplate.postForEntity("https://api.mailgun.net/v3/" + mailGunUserName + "/messages", request, String.class);

            if (U.isEmpty(response) || response.getStatusCodeValue() != HttpStatus.SC_OK) {
                String msg = U.isEmpty(response) ? "response empty" : response.getBody();
                LOGGER.error("send email by Mailgun failed, error msg {}", msg);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 預設建構子生成信件訊息 (包含郵件伺服器登入資訊)
     * @return MimeMessage 多媒體訊息
     * @throws MessagingException 建構訊息時發生的錯誤
     */
    private MimeMessage getMimeMessageByDefault() throws MessagingException {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.port", 25);

        // Get the Session object by Authenticator.
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("username", "password");
            }
        });
        // Get the default Session object.
//        Session session = Session.getDefaultInstance(props);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(FROM));
        message.setSender(new InternetAddress(FROM));

        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO2));

        // Set Subject: header field
        message.setSubject("This is the Subject Line!");

        // Send the actual HTML message, as big as you like
        message.setContent("<h1>This is actual message</h1>", "text/html");

        return message;
    }

    /**
     * 利用 JavaMail 提供的 MimeMessageHelper 生成信件訊息
     * @return MimeMessage 多媒體訊息
     * @throws MessagingException 建構訊息時發生的錯誤
     */
    private MimeMessage getMimeMessageByHelper(String title, String html) throws MessagingException {
        // 建立郵件訊息
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);

        // 設定收件人、寄件人、主題與內文，另外可以 setCc & addCc
        messageHelper.setTo(TO);
        messageHelper.addTo(TO2);   // 增加其他收信人，可以繼續呼叫 addTo( email_address ) 即可
        messageHelper.setFrom(FROM);
        messageHelper.setSubject(title);
        messageHelper.setText(html, true);

        return message;
    }

    /**
     * 將 Handlebars 格式的文件組合資料產生信件內文
     * @param templateType 信件模板
     * @return HTML 文字
     * @throws IOException 讀取模板錯誤
     */
    private String getHtmlByHandlebars(TemplateType templateType) throws IOException {
        Map<String, Object> dataMap = getDataMapByTemplate(templateType);
        String html = "empty";

        switch (templateType) {
            case ACTIVATE:
                html = handlebars
                    .compile("activate-account")
                    .apply(dataMap);
                break;

            case RECALCULATE:
                html = handlebars
                    .compile("recalculate-announce")
                    .apply(dataMap);
                break;

            default:
                break;
        }

        return html;
    }

    /**
     * 將 Jinja Template 格式的文件組合資料產生信件內文
     * @param templateType 信件模板
     * @return HTML 文字
     * @throws IOException 讀取模板錯誤
     */
    private String getHtmlByTemplate(TemplateType templateType) throws IOException {
        Map<String, Object> templateMap = getDataMapByTemplate(templateType);
        Writer writer = new StringWriter();
        String html = "empty";

        switch (templateType) {
            case ACTIVATE:
                activateTemplate.evaluate(writer, templateMap);
                html = writer.toString();
                break;

            case RECALCULATE:
                recalculateTemplate.evaluate(writer, templateMap);
                html = writer.toString();
                break;

            default:
                break;
        }

        return html;
    }

    /**
     * 假資料組成使用
     */
    private Map<String, Object> getTxnObjectMap(String name, String bet, BigDecimal oldPrize) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("accountName", name);
        objectMap.put("validBet", new BigDecimal(bet));
        objectMap.put("beforePrizeWon", oldPrize);
        objectMap.put("afterPrizeWon", BigDecimal.TEN.add(oldPrize));
        return objectMap;
    }

    /**
     * 根據不同模板產生不同假資料
     * @param templateType 信件模板
     * @return 假資料
     */
    private Map<String, Object> getDataMapByTemplate(TemplateType templateType) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userName", "Guest");
        dataMap.put("applicationName", "Google");

        switch (templateType) {
            case ACTIVATE:
                dataMap.put("account_activation_url", "https://google.com.tw");
                break;

            case RECALCULATE:
                dataMap.put("gameId", "CQSSC");
                dataMap.put("drawIdString", "309281");
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(getTxnObjectMap("AAA", "10.52", BigDecimal.ZERO));
                list.add(getTxnObjectMap("BBB", "30.0", BigDecimal.ONE));
                dataMap.put("txn", list);
                break;

            default:
                break;
        }

        return dataMap;
    }
}

