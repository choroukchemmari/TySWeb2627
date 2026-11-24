import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';
import { LoginRequest } from '../model/LoginRequest';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  private usuarioService = inject(UsuarioService);

  correo = '';
  password = '';
  mensajeError = '';
  mensajeExito = '';
  cargando = false;

  login() {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.cargando = true;

    const request: LoginRequest = {
      correo: this.correo,
      password: this.password
    };

    this.usuarioService.login(request).subscribe({
      next: (respuesta) => {
        this.cargando = false;
        sessionStorage.setItem('tokenSesion', respuesta.token);
        this.mensajeExito = `¡Hola ${respuesta.nombre}, has entrado correctamente!`;
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;

        if (error.status === 403) {
          this.mensajeError = 'Tu cuenta todavía no ha sido confirmada. Revisa tu correo.';
        } else if (error.status === 401) {
          this.mensajeError = 'Correo o contraseña incorrectos.';
        } else {
          this.mensajeError = 'Ha ocurrido un error. Inténtalo de nuevo.';
        }
      }
    });
  }
}