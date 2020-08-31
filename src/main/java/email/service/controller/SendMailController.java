package email.service.controller;

import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import email.service.enumeration.TemplateType;
import email.service.service.SendMailService;

@RestController
public class SendMailController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMailController.class);

    @Autowired
    private SendMailService sendMailService;

    @PutMapping("send/sendGrid/{template}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendGrid(@PathVariable("template") TemplateType templateType) {
        sendMailService.sendHTMLEmailBySendGrid(templateType);
        LOGGER.info("send email by sendgrid!");
    }

    @PutMapping("send/javaMail/{template}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendJavaMail(@PathVariable("template") TemplateType templateType) {
        sendMailService.sendHTMLEmailByJavaMail(templateType);
        LOGGER.info("send email by JavaMail!");
    }
}
