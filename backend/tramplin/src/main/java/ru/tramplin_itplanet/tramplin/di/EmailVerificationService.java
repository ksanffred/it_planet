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

    @Value("${app.base-url:https://tramplin-itplanet.ru}")
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
            helper.setText(
                    "<p>Здравствуйте!</p>" +
                    "<p>Для подтверждения email перейдите по ссылке:</p>" +
                    "<p><a href=\"" + verificationLink + "\">" + verificationLink + "</a></p>" +
                    "<p>Ссылка действительна " + ttlHours + " часов.</p>",
                    true
            );
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
}
