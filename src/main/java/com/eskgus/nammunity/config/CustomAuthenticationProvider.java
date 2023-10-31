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

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private BannedUsersService bannedUsersService;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();

        UserDetails user;
        try {
            user = customUserDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new UsernameNotFoundException("ID가 존재하지 않거나 비밀번호가 일치하지 않습니다.");
        }

        if (!bannedUsersService.isAccountNonBanned(username)) {
            throw new BannedException("활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.");
        } else if (!user.isAccountNonLocked()) {
            throw new LockedException("로그인에 5번 이상 실패했습니다. ID 또는 비밀번호 찾기를 하세요.");
        } else if (!user.isEnabled()) {
            throw new DisabledException("이메일 인증이 되지 않은 계정입니다. 이메일 인증을 완료하세요.");
        } else if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("만료된 계정입니다.");
        }

        String password = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("ID가 존재하지 않거나 비밀번호가 일치하지 않습니다.");
        }

        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("만료된 비밀번호입니다.");
        }

        return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
