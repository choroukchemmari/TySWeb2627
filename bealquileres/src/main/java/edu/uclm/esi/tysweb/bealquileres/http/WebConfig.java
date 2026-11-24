package edu.uclm.esi.tysweb.bealquileres.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AutenticacionInterceptor autenticacionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(this.autenticacionInterceptor)
                                .addPathPatterns("/api/vehiculos/asignarVehiculos", "/api/vehiculos/ubicar", "/api/vehiculos/cambiarEstado", "/api/municipios/load", "/api/estaciones/**");    }
}