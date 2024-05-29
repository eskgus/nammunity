package com.eskgus.nammunity.service.email;

import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService implements EmailSender{
    private final JavaMailSender mailSender;
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void send(String email, String text) {
        String title = setTitle(text);

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

    private String setTitle(String text) {
        if (text.contains("이메일 인증")) {
            return "나뮤니티 이메일 인증";
        } else if (text.contains("임시")) {
            return "나뮤니티 임시 비밀번호";
        } else {
            return "나뮤니티 계정 정지";
        }
    }

    public String setConfirmEmailText(String username, String token) {
        String text = "<div style=\"font-size: 18px; font-family: sans-serif\">";

        if (!username.isBlank()) {
            text += "<p>안녕하세요, " + username + "님?</p>" +
                    "<p>나뮤니티 가입을 환영합니다!</p>";
        }
        text += "<p>아래의 링크를 눌러 이메일 인증을 해주세요 ^_^</p>" +
                "<p><a href=\"http://localhost:8080/api/users/confirm?token=" + token + "\">인증하기</a></p>" +
                "<p>링크는 3분 뒤 만료됩니다.</p></div>";
        return text;
    }

    public String setRandomPasswordEmailText(String password) {
        return "<div style=\"font-size: 18px; font-family: sans-serif\">" +
                "<p>임시 비밀번호를 복사해서 로그인해 주세요.</p>" +
                password + "</div>";
    }

    public String setBannedUserEmailText(BannedUsersEmailDto emailDto) {
        return "<div style=\"font-size: 18px; font-family: sans-serif\">" +
                "<p>안녕하세요, " + emailDto.getUsername() + "님</p>" +
                "<p>회원님의 나뮤니티 계정이 " + emailDto.getPeriod() + "(" + emailDto.getStartedDate() +
                " - " + emailDto.getExpiredDate() + ")" + " 정지되었습니다. (사유 - " + emailDto.getReason() + ")</p>" +
                "<p>해당 기간 동안 로그인이 금지되며, 이의 제기 또는 문의는 본 이메일로 하면 됩니다.</p>";
    }
}
