package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.exception.BusinessException;
import ma.projet.events.exception.ConflictException;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.layout.AuthLayout;

@Route(value = "register", layout = AuthLayout.class)
@PageTitle("Create Account - EventReserve")
@AnonymousAllowed
public class RegisterView extends Div {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        /* =========================
           CARD
           ========================= */
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle()
                .set("width", "100%")
                .set("max-width", "520px")
                .set("padding", "2.5rem 2.25rem");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.getStyle().set("gap", "1.4rem");

        /* =========================
           HEADER
           ========================= */
        Icon icon = VaadinIcon.CALENDAR.create();
        icon.setSize("42px");
        icon.getStyle().set("color", "var(--festivent-primary)");

        H2 title = new H2("Create Account");
        title.getStyle()
                .set("margin", "0")
                .set("font-weight", "700")
                .set("font-size", "2rem")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Join EventReserve to discover and book events");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "1rem");

        /* =========================
           FORM GRID
           ========================= */
        Div formGrid = new Div();
        formGrid.addClassName("festivent-form-grid");

        TextField firstName = new TextField("First Name");
        firstName.setRequired(true);
        firstName.setWidthFull();

        TextField lastName = new TextField("Last Name");
        lastName.setRequired(true);
        lastName.setWidthFull();

        EmailField email = new EmailField("Email");
        email.setRequired(true);
        email.setWidthFull();

        TextField phone = new TextField("Phone (optional)");
        phone.setWidthFull();

        PasswordField password = new PasswordField("Password");
        password.setRequired(true);
        password.setRevealButtonVisible(true);
        password.setWidthFull();

        PasswordField confirmPassword = new PasswordField("Confirm Password");
        confirmPassword.setRequired(true);
        confirmPassword.setRevealButtonVisible(true);
        confirmPassword.setWidthFull();

        formGrid.add(
                firstName,
                lastName,
                email,
                phone,
                password,
                confirmPassword
        );

        /* =========================
           SUBMIT BUTTON
           ========================= */
        Button createAccount = new Button("Create Account");
        createAccount.setWidthFull();
        createAccount.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_LARGE
        );

        createAccount.addClickListener(e -> {

            // Validation UI simple
            if (firstName.isEmpty() || lastName.isEmpty()
                    || email.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Notification.show(
                        "Please fill in all required fields",
                        3500,
                        Notification.Position.TOP_CENTER
                );
                return;
            }

            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show(
                        "Passwords do not match",
                        3500,
                        Notification.Position.TOP_CENTER
                );
                return;
            }

            try {
                // Construire l'utilisateur
                User user = new User();
                user.setPrenom(firstName.getValue());
                user.setNom(lastName.getValue());
                user.setEmail(email.getValue());
                user.setTelephone(phone.getValue());
                user.setPassword(password.getValue());
                user.setRole(Role.CLIENT); // inscription publique

                // 🔐 Enregistrement réel en base
                userService.register(user);

                Notification.show(
                        "Account created successfully. Please sign in.",
                        2500,
                        Notification.Position.TOP_CENTER
                );

                // 🔁 Redirection vers login
                UI.getCurrent().navigate("login");

            } catch (ConflictException ex) {
                Notification.show(
                        ex.getMessage(),
                        4000,
                        Notification.Position.TOP_CENTER
                );
            } catch (BusinessException ex) {
                Notification.show(
                        ex.getMessage(),
                        4000,
                        Notification.Position.TOP_CENTER
                );
            } catch (Exception ex) {
                Notification.show(
                        "Unexpected error occurred. Please try again.",
                        4000,
                        Notification.Position.TOP_CENTER
                );
            }
        });

        /* =========================
           FOOTER LINK
           ========================= */
        Span loginPrompt = new Span();
        loginPrompt.getStyle()
                .set("margin-top", "1.5rem")
                .set("font-size", "1rem");

        Anchor loginLink = new Anchor("login", "Sign in");
        loginLink.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("font-weight", "600")
                .set("text-decoration", "none");

        loginPrompt.add(new Text("Already have an account? "), loginLink);

        /* =========================
           ASSEMBLY
           ========================= */
        content.add(
                icon,
                title,
                subtitle,
                formGrid,
                createAccount,
                loginPrompt
        );

        card.add(content);
        add(card);
    }
}
