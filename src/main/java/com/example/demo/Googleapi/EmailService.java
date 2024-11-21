package com.example.demo.Googleapi;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void EmailService(String to, String subject, String text, boolean isHtmlContent) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(to);                        // Define o destinatário
        helper.setSubject(subject);              // Define o assunto
        helper.setText(text, isHtmlContent);     // Define o corpo do e-mail e se é HTML ou não

        emailSender.send(message);            // Envia o e-mail
    }
}