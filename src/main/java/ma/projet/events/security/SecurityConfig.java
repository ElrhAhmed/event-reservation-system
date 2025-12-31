package ma.projet.events.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import ma.projet.events.ui.view.publicview.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        new AntPathRequestMatcher("/"),
                        new AntPathRequestMatcher("/login"),
                        new AntPathRequestMatcher("/register"),
                        new AntPathRequestMatcher("/events/**"),
                        new AntPathRequestMatcher("/event/**"),
                        new AntPathRequestMatcher("/images/**"),
                        new AntPathRequestMatcher("/icons/**"),
                        new AntPathRequestMatcher("/h2-console/**"),
                        new AntPathRequestMatcher("/VAADIN/**")
                ).permitAll()
        );

        super.configure(http);

        setLoginView(http, LoginView.class);


        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/")
                .permitAll()
        );

        // Gestion de la redirection après login
        http.formLogin(form -> form
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                        String targetUrl = "/";

                        if (roles.contains("ROLE_ADMIN")) {
                            targetUrl = "/admin/dashboard";
                        } else if (roles.contains("ROLE_ORGANIZER")) {
                            targetUrl = "/organizer/dashboard";
                        } else if (roles.contains("ROLE_CLIENT")) {
                            targetUrl = "/dashboard";
                        }

                        response.sendRedirect(targetUrl);
                    }
                })
        );

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
        );

        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
        );
    }
}