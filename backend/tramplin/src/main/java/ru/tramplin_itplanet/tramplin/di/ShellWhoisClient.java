package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ShellWhoisClient implements WhoisClient {

    private static final Logger log = LoggerFactory.getLogger(ShellWhoisClient.class);
    private static final Pattern TAXPAYER_ID_PATTERN = Pattern.compile("(?im)^taxpayer-id\\s*:\\s*([0-9]+)\\s*$");
    private static final long WHOIS_TIMEOUT_SECONDS = 10L;

    @Override
    public Optional<String> findTaxpayerIdByDomain(String domain) {
        Process process = null;
        try {
            process = new ProcessBuilder("whois", domain).start();
            boolean finished = process.waitFor(WHOIS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("whois timed out for domain={}", domain);
                return Optional.empty();
            }
            String output = readOutput(process);
            Matcher matcher = TAXPAYER_ID_PATTERN.matcher(output);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
            log.warn("taxpayer-id not found in whois output for domain={}", domain);
            return Optional.empty();
        } catch (IOException e) {
            log.error("Failed to execute whois for domain={}: {}", domain, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("whois interrupted for domain={}", domain);
            return Optional.empty();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        }
        return output.toString();
    }
}
