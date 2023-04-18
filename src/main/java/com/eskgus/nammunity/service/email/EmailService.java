package com.eskgus.nammunity.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EmailService implements EmailSender{
    private final JavaMailSender mailSender;
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String from;

    @Transactional
    @Override
    public void send(String email, String text) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(email);
            helper.setFrom(from);
            helper.setSubject("나뮤니티 이메일 인증");
            helper.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            LOGGER.error("failed to send email", ex);
            throw new IllegalStateException("이메일 전송 실패");
        }
    }
}
