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
        String title;
        if (text.contains("이메일 인증")) {
            title = "나뮤니티 이메일 인증";
        } else {
            title = "나뮤니티 임시 비밀번호";
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(email);
            helper.setFrom(from);
            helper.setSubject(title);
            helper.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            LOGGER.error("failed to send email", ex);
            throw new IllegalStateException("이메일 전송 실패");
        }
    }

    public String setEmailText(String username, String token) {
        return "<div style=\"font-size: 18px; font-family: sans-serif\">" +
                "<p>안녕하세요, " + username + "님?</p>" +
                "<p>나뮤니티 가입을 환영합니다! 아래의 링크를 눌러 이메일 인증을 해주세요 ^_^</p>" +
                "<p><a href=\"http://localhost:8080/api/users/confirm?token=" + token + "\">인증하기</a></p>" +
                "<p>링크는 3분 뒤 만료됩니다.</p></div>";
    }

    public String setEmailText(String password) {
        return "<div style=\"font-size: 18px; font-family: sans-serif\">" +
                "<p>임시 비밀번호를 복사해서 로그인해 주세요.</p>" +
                password + "</div>";
    }
}
