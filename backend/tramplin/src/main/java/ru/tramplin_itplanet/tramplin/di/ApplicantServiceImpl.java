package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
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
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantService;

import java.util.List;

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
        entity.setMajor(command.major());
        entity.setGraduationYear(command.graduationYear());
        entity.setAdditionalEducationDetails(command.additionalEducationDetails());
        entity.setPortfolioUrl(command.portfolioUrl());
        entity.setResumeUrl(command.resumeUrl());
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
                entity.getMajor(),
                entity.getGraduationYear(),
                entity.getAdditionalEducationDetails(),
                entity.getPortfolioUrl(),
                entity.getResumeUrl(),
                skills
        );
    }
}
