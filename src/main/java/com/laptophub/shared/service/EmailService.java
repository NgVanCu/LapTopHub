package com.laptophub.shared.service;

import com.laptophub.shared.properties.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;



import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

// Wrapper mỏng quanh JavaMailSender — không biết gì về nghiệp vụ gọi nó
// (module auth build sẵn subject/htmlBody rồi gọi send()). mailProperties.
// enabled() mặc định false: khi tắt chỉ log nội dung (đủ để test bằng
// Postman ngay, không cần SMTP thật, mvn test/CI không cần credentials thật
// và không có network call). Lỗi gửi (MessagingException khi dựng message,
// MailException khi gửi thật) bị nuốt + log — 1 lần gửi thất bại không được
// làm hỏng request nghiệp vụ đang gọi nó (vd đăng ký tài khoản).
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public EmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public void send(String to, String subject, String htmlBody) {
        if (!mailProperties.enabled()) {
            log.info("[EMAIL disabled — chỉ log, không gửi thật] to={} subject={}\n{}", to, subject, htmlBody);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom(mailProperties.from());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.error("Gửi email tới {} thất bại: {}", to, e.getMessage(), e);
        }
    }
}

