package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new
                UsernameNotFoundException("username not found"));

        List<GrantedAuthority> authorities = createAuthorities(user);

        return new CustomUserDetails(user, authorities);
    }

    private List<GrantedAuthority> createAuthorities(User user) {
        if (user.getRole().equals(Role.ADMIN)) {
            return Collections.singletonList(new SimpleGrantedAuthority(Role.ADMIN.getKey()));
        }
        return Collections.singletonList(new SimpleGrantedAuthority(Role.USER.getKey()));
    }
}
