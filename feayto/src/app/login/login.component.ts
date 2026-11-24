import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioService } from '../services/usuario.service';
import { LoginRequest } from '../model/LoginRequest';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  private usuarioService = inject(UsuarioService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    pwd: ['', Validators.required]
  });

  mensajeError = '';
  cargando = false;

  login() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.mensajeError = '';
    this.cargando = true;

    const { email, pwd } = this.form.getRawValue();
    const request: LoginRequest = { correo: email, password: pwd };

    this.usuarioService.login(request).subscribe({
      next: () => {
        this.cargando = false;
        this.router.navigate(['/menu']);
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