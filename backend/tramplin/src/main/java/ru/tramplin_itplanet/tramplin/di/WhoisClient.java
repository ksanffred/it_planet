package ru.tramplin_itplanet.tramplin.di;

import java.util.Optional;

public interface WhoisClient {
    Optional<WhoisVerificationData> findVerificationDataByDomain(String domain);
}
