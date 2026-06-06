package ru.tramplin_itplanet.tramplin.di;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidVerificationTokenException;

import java.time.Duration;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${app.verification.token-ttl-hours:24}")
    private long ttlHours;

    @Value("${app.base-url:https://backend.tramplin-itplanet.ru}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailVerificationService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(Long userId, String email) {
        String token = UUID.randomUUID().toString();
        String key = redisKey(token);
        redisTemplate.opsForValue().set(key, String.valueOf(userId), Duration.ofHours(ttlHours));
        log.info("Stored verification token for userId={} with TTL={}h", userId, ttlHours);

        String verificationLink = baseUrl + "/auth/verify?token=" + token;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Подтвердите ваш email — Tramplin");
            helper.setText(buildVerificationEmailHtml(verificationLink, ttlHours), true);
            mailSender.send(message);
            log.info("Verification email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
        }
    }

    public Long consumeToken(String token) {
        String key = redisKey(token);
        String userIdStr = redisTemplate.opsForValue().get(key);
        if (userIdStr == null) {
            log.warn("Verification token not found or expired: {}", token);
            throw new InvalidVerificationTokenException();
        }
        redisTemplate.delete(key);
        log.info("Verification token consumed for userId={}", userIdStr);
        return Long.parseLong(userIdStr);
    }

    private String redisKey(String token) {
        return "email:verify:" + token;
    }

    private String buildVerificationEmailHtml(String verificationLink, long ttlHours) {
        String svgIllustration =
            "<svg width=\"100%\" height=\"140\" viewBox=\"0 0 508 140\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<circle cx=\"52\" cy=\"52\" r=\"36\" fill=\"#FF5F00\"/>" +
            "<circle cx=\"170\" cy=\"38\" r=\"28\" fill=\"#0049B8\"/>" +
            "<rect x=\"80\" y=\"76\" width=\"120\" height=\"18\" rx=\"9\" fill=\"#558B6E\"/>" +
            "<rect x=\"110\" y=\"102\" width=\"90\" height=\"10\" rx=\"5\" fill=\"#423C37\"/>" +
            "<circle cx=\"310\" cy=\"100\" r=\"22\" fill=\"none\" stroke=\"#FF5F00\" stroke-width=\"10\"/>" +
            "<path d=\"M240 140 A30 30 0 0 1 300 140\" fill=\"none\" stroke=\"#0049B8\" stroke-width=\"12\" stroke-linecap=\"round\"/>" +
            "<rect x=\"330\" y=\"88\" width=\"44\" height=\"44\" rx=\"10\" fill=\"#558B6E\"/>" +
            "<rect x=\"390\" y=\"70\" width=\"70\" height=\"68\" rx=\"10\" fill=\"#0049B8\"/>" +
            "<circle cx=\"425\" cy=\"104\" r=\"18\" fill=\"#F1EBBB\"/>" +
            "<text x=\"0\" y=\"136\" font-family=\"Arial,sans-serif\" font-size=\"11\" font-weight=\"700\" " +
            "letter-spacing=\"3\" fill=\"#423C37\">ТРАМПЛИН</text>" +
            "</svg>";
    
        return "<!DOCTYPE html>" +
            "<html lang=\"ru\"><head><meta charset=\"UTF-8\"/>" +
            "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"/>" +
            "</head>" +
            "<body style=\"margin:0;padding:0;background-color:#F8F5DD;font-family:Arial,sans-serif;\">" +
    
            "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"" +
            "       style=\"background-color:#F8F5DD;padding:40px 16px;\">" +
            "<tr><td align=\"center\">" +
    
            "<table width=\"580\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"" +
            "       style=\"max-width:580px;width:100%;background-color:#FCFAEE;" +
            "              border-radius:20px;overflow:hidden;border:1px solid #e8e3c8;\">" +
    
            // Illustration header
            "<tr><td style=\"background-color:#F1EBBB;padding:32px 36px 28px;\">" +
            svgIllustration +
            "</td></tr>" +
    
            // Orange accent bar
            "<tr><td style=\"background-color:#FF5F00;height:4px;font-size:0;line-height:0;\">&nbsp;</td></tr>" +
    
            // Body
            "<tr><td style=\"padding:36px 40px 32px;\">" +
    
            "<p style=\"margin:0 0 4px;font-family:Arial,sans-serif;font-size:11px;" +
            "           font-weight:700;letter-spacing:2px;color:#FF5F00;text-transform:uppercase;\">Tramplin</p>" +
    
            "<p style=\"margin:0 0 10px;font-family:Georgia,serif;font-size:26px;" +
            "           font-weight:700;color:#161413;letter-spacing:-0.5px;\">Подтвердите ваш email</p>" +
    
            "<p style=\"margin:0 0 28px;font-family:Arial,sans-serif;font-size:14px;" +
            "           line-height:1.6;color:#423C37;\">" +
            "Вы зарегистрировались на платформе <strong style=\"color:#0049B8;\">Tramplin</strong>. " +
            "Осталось один шаг — подтвердите адрес электронной почты, чтобы активировать аккаунт." +
            "</p>" +
    
            // CTA Button
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin:0 0 20px;\">" +
            "<tr><td style=\"background-color:#0049B8;border-radius:10px;\">" +
            "  <a href=\"" + verificationLink + "\"" +
            "     style=\"display:inline-block;padding:15px 40px;" +
            "            font-family:Arial,sans-serif;font-size:15px;font-weight:700;" +
            "            color:#F1EBBB;text-decoration:none;letter-spacing:0.2px;border-radius:10px;\">" +
            "    Подтвердить email \u2192" +
            "  </a>" +
            "</td></tr></table>" +
    
            // Divider
            "<hr style=\"border:none;border-top:1px solid #E8E3C8;margin:0 0 18px;\"/>" +
    
            // Fallback link
            "<p style=\"margin:0 0 6px;font-family:Arial,sans-serif;font-size:12px;color:#423C37;line-height:1.5;\">" +
            "Если кнопка не работает, скопируйте ссылку в браузер:</p>" +
            "<p style=\"margin:0 0 22px;word-break:break-all;\">" +
            "  <a href=\"" + verificationLink + "\"" +
            "     style=\"font-family:Arial,sans-serif;font-size:11px;color:#0049B8;text-decoration:underline;\">" +
            verificationLink +
            "  </a></p>" +
    
            // TTL notice
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr>" +
            "<td style=\"background-color:#F1EBBB;border-radius:10px;padding:13px 16px;" +
            "            border-left:4px solid #558B6E;\">" +
            "  <p style=\"margin:0;font-family:Arial,sans-serif;font-size:13px;color:#423C37;line-height:1.5;\">" +
            "    Ссылка действительна <strong>" + ttlHours + " часа</strong>. " +
            "    Если вы не регистрировались \u2014 просто проигнорируйте это письмо." +
            "  </p></td></tr></table>" +
    
            "</td></tr>" +
    
            // Footer
            "<tr><td style=\"background-color:#F1EBBB;padding:18px 40px;border-top:1px solid #E8E3C8;\">" +
            "  <p style=\"margin:0;font-family:Arial,sans-serif;font-size:11px;" +
            "             color:#423C37;text-align:center;line-height:1.6;\">" +
            "    \u00a9 2025 Tramplin \u2014 Это автоматическое письмо, отвечать на него не нужно." +
            "  </p></td></tr>" +
    
            "</table>" +
            "</td></tr></table>" +
            "</body></html>";
    }
}
