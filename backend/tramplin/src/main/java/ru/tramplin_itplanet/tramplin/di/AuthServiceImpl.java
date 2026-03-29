package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.tramplin_itplanet.tramplin.domain.exception.UserAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.User;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.repository.UserRepository;
import ru.tramplin_itplanet.tramplin.domain.service.AuthService;
import ru.tramplin_itplanet.tramplin.web.dto.AuthResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CurrentUserResponse;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final EmailVerificationService emailVerificationService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           UserDetailsService userDetailsService,
                           EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.emailVerificationService = emailVerificationService;
    }

    @Override
    public AuthResponse register(String email, String displayName, String password, UserRole role) {
        UserRole effectiveRole = role != null ? role : UserRole.APPLICANT;
        if (effectiveRole == UserRole.CURATOR) {
            throw new AccessDeniedException("CURATOR role cannot be registered via public endpoint");
        }
        log.info("Registering new user: email={}, role={}", email, effectiveRole);
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration failed — user already exists: {}", email);
            throw new UserAlreadyExistsException(email);
        }
        String passwordHash = passwordEncoder.encode(password);
        User user = userRepository.save(email, displayName, passwordHash, effectiveRole);
        emailVerificationService.sendVerificationEmail(user.id(), user.email());
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);
        log.info("User registered successfully: id={}, email={}", user.id(), user.email());
        return new AuthResponse(token, user.id(), user.email(), user.displayName(), user.role().name());
    }

    @Override
    public AuthResponse login(String email, String password) {
        log.info("Login attempt for email: {}", email);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.warn("Login failed — invalid password for email: {}", email);
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtService.generateToken(userDetails);
        User user = userRepository.findByEmail(email).orElseThrow();
        log.info("User logged in successfully: email={}", email);
        return new AuthResponse(token, user.id(), user.email(), user.displayName(), user.role().name());
    }

    @Override
    public CurrentUserResponse getCurrentUser(String email) {
        log.debug("Fetching current user by email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid authentication token"));
        return new CurrentUserResponse(
                user.id(),
                user.email(),
                user.displayName(),
                user.role().name(),
                user.isVerified()
        );
    }

    @Override
    public void verifyEmail(String token) {
        Long userId = emailVerificationService.consumeToken(token);
        userRepository.verifyUser(userId);
        log.info("Email verified for userId={}", userId);
    }
}
