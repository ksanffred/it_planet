package ru.tramplin_itplanet.tramplin.di;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidVerificationTokenException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new EmailVerificationService(redisTemplate, mailSender);
        // inject @Value fields via reflection
        setField(service, "ttlHours", 24L);
        setField(service, "baseUrl", "https://tramplin-itplanet.ru");
        setField(service, "fromAddress", "noreply@tramplin-itplanet.ru");
    }

    @Test
    void sendVerificationEmail_storesTokenInRedisAndSendsEmail() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendVerificationEmail(42L, "user@example.com");

        // Redis key set with 24h TTL
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(keyCaptor.capture(), valueCaptor.capture(), eq(Duration.ofHours(24)));
        assertThat(keyCaptor.getValue()).startsWith("email:verify:");
        assertThat(valueCaptor.getValue()).isEqualTo("42");

        // Mail sent
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void consumeToken_validToken_returnsUserIdAndDeletesKey() {
        when(valueOps.get("email:verify:test-token")).thenReturn("7");

        Long userId = service.consumeToken("test-token");

        assertThat(userId).isEqualTo(7L);
        verify(redisTemplate).delete("email:verify:test-token");
    }

    @Test
    void consumeToken_expiredOrInvalidToken_throwsInvalidVerificationTokenException() {
        when(valueOps.get("email:verify:bad-token")).thenReturn(null);

        assertThatThrownBy(() -> service.consumeToken("bad-token"))
                .isInstanceOf(InvalidVerificationTokenException.class)
                .hasMessageContaining("expired");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
