package ma.projet.events.ui.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateFormatter {

    private static final Locale LOCALE_FR = Locale.FRENCH;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", LOCALE_FR);

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy à HH:mm", LOCALE_FR);

    private DateFormatter() {
        // util class
    }

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "-";
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : "-";
    }
}
