package com.eskgus.nammunity.handler;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(basePackages = "com.eskgus.nammunity.web.controller.mvc")
@RequiredArgsConstructor
public class CustomControllerAdvice {
    @Autowired
    private PrincipalHelper principalHelper;

    @ModelAttribute
    public void addDefaultAttributes(Principal principal, Model model, HttpServletRequest request) {
        Map<String, Object> attr = new HashMap<>();
        addAuthAndAdmin(principal, attr);
        addPrePage(request, attr);

        model.addAllAttributes(attr);
    }

    private void addAuthAndAdmin(Principal principal, Map<String, Object> attr) {
        User user = principalHelper.getUserFromPrincipal(principal, false);
        if (user != null) {
            attr.put("auth", user.getNickname());

            if (user.getRole().equals(Role.ADMIN)) {
                attr.put("admin", true);
            }
        }
    }

    private void addPrePage(HttpServletRequest request, Map<String, Object> attr) {
        Object url = request.getSession().getAttribute("prePage");
        if (url != null) {
            attr.put("prePage", url.toString());
        } else {
            attr.put("prePage", "/");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException(IllegalArgumentException ex) {
        System.out.println("ControllerAdvice.handleIllegalArgumentException().....");
    }
}
