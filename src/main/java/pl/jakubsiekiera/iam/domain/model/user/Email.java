package pl.jakubsiekiera.iam.domain.model.user;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || !EMAIL_REGEX.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}