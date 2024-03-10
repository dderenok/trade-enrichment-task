package com.verygoodbank.tes.web.validation;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static java.time.LocalDate.parse;

/**
 * DateFormatValidator is a utility class for validating date strings against a specified
 * date format.
 * It encapsulates a {@link DateTimeFormatter} that defines the expected format of the date string.
 *
 * <p> The primary responsibility of this class is to determine if a given date string
 * conforms to the format specified by the {@code DateTimeFormatter} provided at instantiation.
 */
public class DateFormatValidator {
    private final DateTimeFormatter dateFormatter;

    public DateFormatValidator(DateTimeFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public boolean isValid(String date) {
        try {
            parse(date, this.dateFormatter);
        } catch (DateTimeParseException exception) {
            System.err.println("An error occurred during date parsing " + exception.getMessage());
            return false;
        }
        return true;
    }
}
