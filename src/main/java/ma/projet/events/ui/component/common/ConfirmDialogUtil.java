package ma.projet.events.ui.component.common;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public final class ConfirmDialogUtil {

    private ConfirmDialogUtil() {
    }

    public static void show(
            String title,
            String message,
            Runnable onConfirm
    ) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(title);
        dialog.setText(message);

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Confirmer");
        dialog.addConfirmListener(e -> onConfirm.run());

        dialog.open();
    }
}
