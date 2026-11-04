package com.grouploja.basic_shirt_app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estaticos e rotas publicas de visualizacao e autenticacao
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/", "/catalogo", "/catalogo/**").permitAll()
                .requestMatchers("/login", "/cadastro", "/acesso-negado").permitAll()

                // Consulta publica da API do catalogo
                .requestMatchers(HttpMethod.GET, "/api/produtos", "/api/produtos/**", "/api/categorias", "/api/categorias/**").permitAll()

                // Rotas exclusivas para administradores (Painel Admin e escrita de catalogo/usuarios)
                .requestMatchers("/admin", "/admin/**", "/h2-console/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/produtos", "/api/produtos/**", "/api/categorias", "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/produtos", "/api/produtos/**", "/api/categorias", "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos", "/api/produtos/**", "/api/categorias", "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")

                // Fluxo de compras e perfil do cliente exigem usuario autenticado
                .requestMatchers("/carrinho", "/carrinho/**", "/checkout", "/checkout/**", "/pedidos", "/pedidos/**", "/minha-conta", "/minha-conta/**").authenticated()
                .requestMatchers("/api/carrinho/**", "/api/pedidos/**").authenticated()

                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .defaultSuccessUrl("/catalogo", false)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/acesso-negado")
            );

        return http.build();
    }
}
