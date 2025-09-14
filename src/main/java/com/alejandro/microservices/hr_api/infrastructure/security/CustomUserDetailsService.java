package com.alejandro.microservices.hr_api.infrastructure.security;

import com.alejandro.microservices.hr_api.domain.model.User;
import com.alejandro.microservices.hr_api.domain.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación personalizada de UserDetailsService para Spring Security.
 *
 * Carga usuarios desde la base de datos y los convierte en objetos UserDetails
 * para la autenticación y autorización con Spring Security.
 *
 * @author Sistema HR API
 * @version 1.0
 * @since 2025-01-14
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new RuntimeException("Username cannot be null");
        }

        User user = userRepository.findByUsernameAndEnabled(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new CustomUserPrincipal(user);
    }

    /**
     * Implementación personalizada de UserDetails que encapsula un User.
     */
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            String role = user.getRole();
            if (role == null || role.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.isEnabled();
        }
    }
}
