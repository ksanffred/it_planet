package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaTagRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.UserNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateCurrentApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantService;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class ApplicantServiceImpl implements ApplicantService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantServiceImpl.class);

    private final JpaApplicantRepository jpaApplicantRepository;
    private final JpaUserRepository jpaUserRepository;
    private final JpaTagRepository jpaTagRepository;

    public ApplicantServiceImpl(JpaApplicantRepository jpaApplicantRepository,
                                JpaUserRepository jpaUserRepository,
                                JpaTagRepository jpaTagRepository) {
        this.jpaApplicantRepository = jpaApplicantRepository;
        this.jpaUserRepository = jpaUserRepository;
        this.jpaTagRepository = jpaTagRepository;
    }

    @Override
    @Transactional
    public ApplicantProfile create(CreateApplicantCommand command) {
        log.info("Creating applicant profile for userId={}", command.userId());

        UserEntity user = jpaUserRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (user.getRole() != UserRole.APPLICANT) {
            log.warn("Applicant profile creation forbidden: userId={}, role={}", user.getId(), user.getRole());
            throw new AccessDeniedException("User role must be APPLICANT");
        }

        if (jpaApplicantRepository.findByUserIdWithSkills(user.getId()).isPresent()) {
            log.warn("Applicant profile already exists for userId={}", user.getId());
            throw new ApplicantAlreadyExistsException(user.getId());
        }

        List<TagEntity> skills = command.skillTagIds().isEmpty()
                ? List.of()
                : jpaTagRepository.findAllById(command.skillTagIds());

        ApplicantEntity entity = new ApplicantEntity();
        entity.setUserId(user.getId());
        entity.setName(user.getDisplayName());
        entity.setUniversity(command.university());
        entity.setFaculty(command.faculty());
        entity.setCurrentFieldOfStudy(command.currentFieldOfStudy());
        entity.setDesiredPosition(command.desiredPosition());
        entity.setMajor(command.major());
        entity.setGraduationYear(command.graduationYear());
        entity.setAdditionalEducationDetails(command.additionalEducationDetails());
        entity.setPortfolioUrl(command.portfolioUrl());
        entity.setResumeUrl(command.resumeUrl());
        entity.setVisibility(ApplicantVisibility.PRIVATE);
        entity.setSkills(skills);

        ApplicantEntity saved = jpaApplicantRepository.save(entity);
        return toProfile(saved);
    }

    @Override
    public ApplicantProfile getById(Long id) {
        log.info("Loading applicant profile by id={}", id);
        ApplicantEntity entity = jpaApplicantRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new ApplicantNotFoundException(id));
        return toProfile(entity);
    }

    @Override
    public ApplicantProfile getCurrentByUserEmail(String email) {
        log.info("Loading current applicant profile by email={}", email);
        UserEntity user = resolveAuthenticatedUserByEmail(email);
        ApplicantEntity entity = findApplicantByUserId(user.getId());
        return toProfile(entity);
    }

    @Override
    @Transactional
    public ApplicantProfile update(Long id, UpdateApplicantCommand command) {
        log.info("Updating applicant profile id={}, userId={}", id, command.userId());

        ApplicantEntity entity = jpaApplicantRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new ApplicantNotFoundException(id));

        UserEntity user = jpaUserRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (user.getRole() != UserRole.APPLICANT) {
            log.warn("Applicant profile update forbidden: userId={}, role={}", user.getId(), user.getRole());
            throw new AccessDeniedException("User role must be APPLICANT");
        }

        jpaApplicantRepository.findByUserIdWithSkills(user.getId())
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .ifPresent(existing -> {
                    throw new ApplicantAlreadyExistsException(user.getId());
                });

        List<TagEntity> skills = command.skillTagIds().isEmpty()
                ? List.of()
                : jpaTagRepository.findAllById(command.skillTagIds());

        entity.setUserId(user.getId());
        entity.setName(command.name());
        entity.setUniversity(command.university());
        entity.setFaculty(command.faculty());
        entity.setCurrentFieldOfStudy(command.currentFieldOfStudy());
        entity.setDesiredPosition(command.desiredPosition());
        entity.setMajor(command.major());
        entity.setGraduationYear(command.graduationYear());
        entity.setAdditionalEducationDetails(command.additionalEducationDetails());
        entity.setPortfolioUrl(command.portfolioUrl());
        entity.setResumeUrl(command.resumeUrl());
        entity.setSkills(skills);

        ApplicantEntity updated = jpaApplicantRepository.save(entity);
        return toProfile(updated);
    }

    @Override
    @Transactional
    public ApplicantProfile updateCurrentByUserEmail(String email, UpdateCurrentApplicantCommand command) {
        log.info("Updating current applicant profile by email={}", email);
        UserEntity user = resolveAuthenticatedUserByEmail(email);
        ApplicantEntity entity = findApplicantByUserId(user.getId());

        List<TagEntity> skills = command.skillTagIds().isEmpty()
                ? List.of()
                : jpaTagRepository.findAllById(command.skillTagIds());

        entity.setName(command.name());
        entity.setUniversity(command.university());
        entity.setFaculty(command.faculty());
        entity.setCurrentFieldOfStudy(command.currentFieldOfStudy());
        entity.setDesiredPosition(command.desiredPosition());
        entity.setMajor(command.major());
        entity.setGraduationYear(command.graduationYear());
        entity.setAdditionalEducationDetails(command.additionalEducationDetails());
        entity.setPortfolioUrl(command.portfolioUrl());
        entity.setResumeUrl(command.resumeUrl());
        entity.setSkills(skills);

        ApplicantEntity updated = jpaApplicantRepository.save(entity);
        return toProfile(updated);
    }

    @Override
    @Transactional
    public ApplicantProfile updateVisibilityByUserEmail(String email, ApplicantVisibility visibility) {
        log.info("Updating applicant visibility by email={}, visibility={}", email, visibility);
        UserEntity user = resolveAuthenticatedUserByEmail(email);

        if (user.getRole() != UserRole.APPLICANT) {
            throw new AccessDeniedException("User role must be APPLICANT");
        }

        ApplicantEntity entity = findApplicantByUserId(user.getId());
        entity.setVisibility(visibility);

        ApplicantEntity updated = jpaApplicantRepository.save(entity);
        return toProfile(updated);
    }

    private UserEntity resolveAuthenticatedUserByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found by email={}", email);
                    return new BadCredentialsException("Invalid authentication token");
                });
    }

    private ApplicantEntity findApplicantByUserId(Long userId) {
        return jpaApplicantRepository.findByUserIdWithSkills(userId)
                .orElseThrow(() -> {
                    log.warn("Applicant profile not found for userId={}", userId);
                    return new ApplicantNotFoundException(userId);
                });
    }

    private static ApplicantProfile toProfile(ApplicantEntity entity) {
        List<Tag> skills = entity.getSkills().stream()
                .map(tag -> new Tag(tag.getId(), tag.getName(), tag.getCategory()))
                .toList();
        return new ApplicantProfile(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getUniversity(),
                entity.getFaculty(),
                entity.getCurrentFieldOfStudy(),
                entity.getDesiredPosition(),
                entity.getMajor(),
                entity.getGraduationYear(),
                entity.getAdditionalEducationDetails(),
                entity.getPortfolioUrl(),
                entity.getAvatarUrl(),
                entity.getResumeUrl(),
                entity.getVisibility() != null ? entity.getVisibility() : ApplicantVisibility.PRIVATE,
                skills
        );
    }
}
