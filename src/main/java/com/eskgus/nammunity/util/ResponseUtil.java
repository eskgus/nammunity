package com.eskgus.nammunity.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

public class ResponseUtil {
    public static void setMessage(AuthenticationException ex, HttpServletRequest request, HttpServletResponse response) {
        String message = ex.getMessage();

        FlashMap flashMap = new FlashMap();
        flashMap.put("message", message);
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
    }

    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = setUrl(request);
        response.sendRedirect(url);
    }

    private static String setUrl(HttpServletRequest request) {
        Object prePage = request.getSession().getAttribute("prePage");
        return prePage != null ? prePage.toString() : "/";
    }
}
