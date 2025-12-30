package ma.projet.events.ui.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class PriceFormatter {

    // Locale Maroc (français)
    private static final Locale LOCALE_MA = new Locale("fr", "MA");

    private PriceFormatter() {
        // util class
    }

    /**
     * Formate un montant en Dirham marocain (MAD).
     * Exemple : 150.00 -> 150,00 MAD
     */
    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }

        NumberFormat format = NumberFormat.getCurrencyInstance(LOCALE_MA);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        return format.format(amount);
    }

    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }
}
