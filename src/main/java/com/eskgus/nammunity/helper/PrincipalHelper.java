package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class PrincipalHelper {
    @Autowired
    private UserService userService;

    public User getUserFromPrincipal(Principal principal) {
        if (principal != null) {
            return userService.findByUsername(principal.getName());
        }
        return null;
    }
}
