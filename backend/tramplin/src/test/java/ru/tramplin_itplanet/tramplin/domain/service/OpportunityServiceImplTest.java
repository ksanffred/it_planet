package ru.tramplin_itplanet.tramplin.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.*;
import ru.tramplin_itplanet.tramplin.domain.repository.OpportunityRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityServiceImplTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @InjectMocks
    private OpportunityServiceImpl opportunityService;

    @Test
    void findAll_returnsOpportunities() {
        List<Opportunity> expected = List.of(buildOpportunity(1L), buildOpportunity(2L));
        when(opportunityRepository.findAll()).thenReturn(expected);

        List<Opportunity> result = opportunityService.findAll();

        assertThat(result).isEqualTo(expected);
        verify(opportunityRepository).findAll();
    }

    @Test
    void getById_existingId_returnsOpportunity() {
        Opportunity expected = buildOpportunity(1L);
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(expected));

        Opportunity result = opportunityService.getById(1L);

        assertThat(result).isEqualTo(expected);
        verify(opportunityRepository).findById(1L);
    }

    @Test
    void getById_nonExistingId_throwsOpportunityNotFoundException() {
        when(opportunityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> opportunityService.getById(99L))
                .isInstanceOf(OpportunityNotFoundException.class)
                .hasMessageContaining("99");

        verify(opportunityRepository).findById(99L);
    }

    @Test
    void create_validCommand_returnsSavedOpportunity() {
        CreateOpportunityCommand command = buildCommand();
        Opportunity expected = buildOpportunity(1L);
        when(opportunityRepository.save(command)).thenReturn(expected);

        Opportunity result = opportunityService.create(command);

        assertThat(result).isEqualTo(expected);
        verify(opportunityRepository).save(command);
    }

    private CreateOpportunityCommand buildCommand() {
        return new CreateOpportunityCommand(
                1L,
                "Java Developer",
                "Backend role",
                OpportunityType.VACANCY,
                OpportunityFormat.REMOTE,
                null,
                "Moscow",
                null,
                null,
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(150_000),
                null,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                OpportunityStatus.ACTIVE,
                List.of(),
                List.of()
        );
    }

    private Opportunity buildOpportunity(Long id) {
        return new Opportunity(
                id,
                new Employer(1L, "Acme Corp", null, "https://acme.com", "hr@acme.com"),
                "Java Developer",
                "Backend role",
                OpportunityType.VACANCY,
                OpportunityFormat.REMOTE,
                null,
                "Moscow",
                null,
                null,
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(150_000),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                OpportunityStatus.ACTIVE,
                List.of(),
                List.of()
        );
    }
}
