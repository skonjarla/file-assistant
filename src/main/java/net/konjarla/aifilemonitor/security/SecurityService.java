package net.konjarla.aifilemonitor.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    private static final Pattern[] SENSITIVE_PATTERNS = new Pattern[]{
        Pattern.compile("password", Pattern.CASE_INSENSITIVE),
        Pattern.compile("api[_-]?key", Pattern.CASE_INSENSITIVE),
        Pattern.compile("secret", Pattern.CASE_INSENSITIVE),
        Pattern.compile("token", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ssn", Pattern.CASE_INSENSITIVE),
        Pattern.compile("credit[ -]?card", Pattern.CASE_INSENSITIVE)
    };

    public boolean containsSensitiveData(String text) {
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            if (pattern.matcher(text).find()) {
                log.warn("Sensitive pattern detected: {}", pattern.pattern());
                return true;
            }
        }
        return false;
    }
} 