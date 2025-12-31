package ma.projet.events.ui.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.common.ConfirmDialogUtil;
import ma.projet.events.ui.layout.AdminLayout;
import ma.projet.events.ui.util.DateFormatter;

import java.util.List;

@Route(value = "admin/users", layout = AdminLayout.class)
@PageTitle("Gestion Utilisateurs | FESTIVENT")
@RolesAllowed("ADMIN")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final SecurityService securityService;

    private Grid<User> grid;
    private GridListDataView<User> dataView;

    // Filtres
    private TextField searchField;
    private ComboBox<Role> roleFilter;
    private ComboBox<String> statusFilter;

    public UserManagementView(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        add(createHeader(), createToolbar());

        // Grid Wrapper
        VerticalLayout gridWrapper = new VerticalLayout();
        gridWrapper.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.SMALL, LumoUtility.BorderRadius.LARGE, LumoUtility.Overflow.HIDDEN);
        gridWrapper.setPadding(false);
        gridWrapper.setSizeFull();

        grid = createGrid();
        gridWrapper.add(grid);

        add(gridWrapper);

        refreshData();
    }

    private Component createHeader() {
        H2 title = new H2("Annuaire Utilisateurs");
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.XLARGE);
        return title;
    }

    private Component createToolbar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher (Nom, Email)...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addClassName(LumoUtility.Flex.GROW);
        searchField.addValueChangeListener(e -> updateFilter());

        roleFilter = new ComboBox<>();
        roleFilter.setPlaceholder("Rôle");
        roleFilter.setItems(Role.values());
        roleFilter.setItemLabelGenerator(Role::getLabel);
        roleFilter.setClearButtonVisible(true);
        roleFilter.setWidth("180px");
        roleFilter.addValueChangeListener(e -> updateFilter());

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems("Actif", "Inactif");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> updateFilter());

        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH), e -> refreshData());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, roleFilter, statusFilter, refreshBtn);
        toolbar.setWidthFull();
        toolbar.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return toolbar;
    }

    private Grid<User> createGrid() {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(User::getNomComplet).setHeader("Utilisateur").setSortable(true).setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(User::getEmail).setHeader("Email").setSortable(true).setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span(user.getRole().getLabel());
            String theme = switch (user.getRole()) {
                case ADMIN -> "badge error";
                case ORGANIZER -> "badge contrast";
                default -> "badge";
            };
            badge.getElement().getThemeList().add(theme);
            return badge;
        })).setHeader("Rôle").setSortable(true).setAutoWidth(true);

        grid.addColumn(u -> DateFormatter.format(u.getDateInscription())).setHeader("Inscription").setSortable(true).setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(user -> {
            boolean active = user.isActif();
            Span badge = new Span(active ? "Actif" : "Inactif");
            badge.getElement().getThemeList().add(active ? "badge success" : "badge error");
            return badge;
        })).setHeader("Statut").setSortable(true).setAutoWidth(true);

        grid.addComponentColumn(this::createActions).setHeader("Actions").setFrozenToEnd(true);

        return grid;
    }

    private Component createActions(User user) {
        HorizontalLayout actions = new HorizontalLayout();

        if (isCurrentUser(user)) return new Span("(Vous)");

        Button roleBtn = new Button(new Icon(VaadinIcon.USER_CARD));
        roleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        roleBtn.setTooltipText("Changer rôle");
        roleBtn.addClickListener(e -> openRoleDialog(user));

        Button toggleStatusBtn;
        if (user.isActif()) {
            toggleStatusBtn = new Button(new Icon(VaadinIcon.BAN));
            toggleStatusBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
            toggleStatusBtn.setTooltipText("Désactiver");
            toggleStatusBtn.addClickListener(e -> handleDeactivation(user));
        } else {
            toggleStatusBtn = new Button(new Icon(VaadinIcon.CHECK_CIRCLE));
            toggleStatusBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON);
            toggleStatusBtn.setTooltipText("Activer");
            toggleStatusBtn.addClickListener(e -> handleActivation(user));
        }

        actions.add(roleBtn, toggleStatusBtn);
        return actions;
    }

    // --- LOGIQUE ---
    private void refreshData() {
        List<User> users = userService.getAllUsers();
        dataView = grid.setItems(users);
        updateFilter();
    }

    private void updateFilter() {
        if (dataView == null) return;
        dataView.setFilter(user -> {
            String search = searchField.getValue().trim().toLowerCase();
            boolean matchSearch = search.isEmpty() || user.getNomComplet().toLowerCase().contains(search) || user.getEmail().toLowerCase().contains(search);
            boolean matchRole = roleFilter.getValue() == null || user.getRole() == roleFilter.getValue();
            boolean matchStatus = statusFilter.getValue() == null ||
                    (statusFilter.getValue().equals("Actif") && user.isActif()) ||
                    (statusFilter.getValue().equals("Inactif") && !user.isActif());
            return matchSearch && matchRole && matchStatus;
        });
    }

    private void openRoleDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Rôle : " + user.getNomComplet());
        ComboBox<Role> roleCombo = new ComboBox<>("Nouveau rôle", Role.values());
        roleCombo.setItemLabelGenerator(Role::getLabel);
        roleCombo.setValue(user.getRole());

        Button save = new Button("Enregistrer", e -> {
            try {
                if (roleCombo.getValue() != null) {
                    userService.changeUserRole(user.getId(), roleCombo.getValue());
                    showSuccess("Rôle mis à jour.");
                    refreshData();
                    dialog.close();
                }
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.add(roleCombo);
        dialog.getFooter().add(new Button("Annuler", e->dialog.close()), save);
        dialog.open();
    }

    private void handleDeactivation(User user) {
        ConfirmDialogUtil.show("Désactiver ?", "L'utilisateur ne pourra plus se connecter.", () -> {
            try {
                userService.deactivateAccount(user.getId(), getCurrentUser().getId());
                showSuccess("Désactivé.");
                refreshData();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private void handleActivation(User user) {
        ConfirmDialogUtil.show("Activer ?", "L'accès sera rétabli.", () -> {
            try {
                userService.activateAccount(user.getId(), getCurrentUser().getId());
                showSuccess("Activé.");
                refreshData();
            } catch (Exception e) { showError(e.getMessage()); }
        });
    }

    private boolean isCurrentUser(User user) {
        User current = getCurrentUser();
        return current != null && current.getId().equals(user.getId());
    }

    private User getCurrentUser() {
        var userDetails = securityService.getAuthenticatedUser();
        return (userDetails != null) ? userService.getUserByEmail(userDetails.getUsername()) : null;
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}