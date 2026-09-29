package edu.uclm.esi.tysweb.beusu.services;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void enviarConfirmacionRegistro(String correo, String token) {
        // TODO: sustituir por envío real de correo (JavaMailSender / SMTP)
        System.out.println("=== EMAIL SIMULADO ===");
        System.out.println("Para: " + correo);
        System.out.println("Asunto: Confirma tu cuenta");
        System.out.println("Enlace de confirmación con token: " + token);
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