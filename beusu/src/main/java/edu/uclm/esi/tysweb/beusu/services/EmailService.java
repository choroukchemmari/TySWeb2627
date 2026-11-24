package edu.uclm.esi.tysweb.beusu.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${beusu.url-publica}")
    private String urlPublica;

    public void enviarConfirmacionRegistro(String correo, String token) {
        // TODO: sustituir por envío real de correo (JavaMailSender / SMTP)
        System.out.println("=== EMAIL SIMULADO ===");
        System.out.println("Para: " + correo);
        System.out.println("Asunto: Confirma tu cuenta");
        System.out.println("Enlace de confirmación: " + this.urlPublica + "/api/cuentas/confirmar?token=" + token);
        System.out.println("======================");
    }

    public void enviarCuentaConfirmada(String correo) {
        System.out.println("=== EMAIL SIMULADO ===");
        System.out.println("Para: " + correo);
        System.out.println("Asunto: Cuenta confirmada");
        System.out.println("Tu cuenta se ha confirmado correctamente.");
        System.out.println("======================");
    }
}