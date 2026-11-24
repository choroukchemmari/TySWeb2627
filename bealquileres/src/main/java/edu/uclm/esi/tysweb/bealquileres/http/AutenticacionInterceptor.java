package edu.uclm.esi.tysweb.bealquileres.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import edu.uclm.esi.tysweb.bealquileres.dto.SesionDto;
import edu.uclm.esi.tysweb.bealquileres.services.BeusuClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AutenticacionInterceptor implements HandlerInterceptor {

    @Autowired
    private BeusuClient beusuClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
            return true;

        String token = request.getHeader("X-Session-Token");
        if (token == null || token.isBlank())
            token = request.getParameter("sessionToken");

        if (token == null || token.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta el token de sesión");

        SesionDto sesion = this.beusuClient.validar(token);
        request.setAttribute("sesion", sesion);

        return true;
    }
}