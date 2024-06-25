package com.eskgus.nammunity.config;

import com.eskgus.nammunity.exception.BannedException;
import com.eskgus.nammunity.service.user.BannedUsersService;
import com.eskgus.nammunity.service.user.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final CustomUserDetailsService customUserDetailsService;
    private final BCryptPasswordEncoder encoder;
    private final BannedUsersService bannedUsersService;

    @Autowired
    public CustomAuthenticationProvider(CustomUserDetailsService customUserDetailsService,
                                        BCryptPasswordEncoder encoder,
                                        BannedUsersService bannedUsersService) {
        this.customUserDetailsService = customUserDetailsService;
        this.encoder = encoder;
        this.bannedUsersService = bannedUsersService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        if (username.isBlank()) {
            throw new AuthenticationServiceException(EMPTY_USERNAME.getMessage());
        }

        String password = authentication.getCredentials().toString();
        if (password.isBlank()) {
            throw new AuthenticationServiceException(EMPTY_PASSWORD.getMessage());
        }

        UserDetails user;
        try {
            user = customUserDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new UsernameNotFoundException(BAD_CREDENTIALS.getMessage());
        }

        if (!bannedUsersService.isAccountNonBanned(username)) {
            throw new BannedException(BANNED_USER.getMessage());
        } else if (!user.isAccountNonLocked()) {
            throw new LockedException(LOCKED_USER.getMessage());
        } else if (!user.isEnabled()) {
            throw new DisabledException(DISABLED.getMessage());
        } else if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException(ACCOUNT_EXPIRED.getMessage());
        }

        if (!this.encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException(BAD_CREDENTIALS.getMessage());
        }

        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException(CREDENTIALS_EXPIRED.getMessage());
        }

        return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
