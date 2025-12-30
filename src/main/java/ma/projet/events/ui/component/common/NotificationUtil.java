package ma.projet.events.ui.component.common;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public final class NotificationUtil {

    private NotificationUtil() {
        // util class
    }

    public static void success(String message, NotificationVariant lumoSuccess) {
        show(message, NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String message) {
        show(message, NotificationVariant.LUMO_ERROR);
    }

    public static void info(String message) {
        show(message, NotificationVariant.LUMO_PRIMARY);
    }

    public static void show(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(variant);
    }
}
