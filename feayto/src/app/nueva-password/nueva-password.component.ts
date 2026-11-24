import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioService } from '../services/usuario.service';

@Component({
  selector: 'app-nueva-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './nueva-password.component.html'
})
export class NuevaPasswordComponent {

  private usuarioService = inject(UsuarioService);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);

  token = this.route.snapshot.queryParamMap.get('token');
  enlaceInvalido = !this.token || this.route.snapshot.queryParamMap.has('error');

  form = this.fb.nonNullable.group({
    password: ['', Validators.required],
    confirmacion: ['', Validators.required]
  });

  cargando = false;
  cambiada = false;
  mensajeError = '';

  cambiar() {
    if (this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }

    this.mensajeError = '';
    this.cargando = true;
    const { password, confirmacion } = this.form.getRawValue();

    this.usuarioService.crearNuevaPassword(this.token, password, confirmacion).subscribe({
      next: () => {
        this.cargando = false;
        this.cambiada = true;
      },
      error: (error: HttpErrorResponse) => {
        this.cargando = false;

        if (error.status === 410) {
          // Mismo mensaje si el token no existe o ha caducado
          this.enlaceInvalido = true;
        } else if (error.status === 400) {
          this.mensajeError = 'Las contraseñas no coinciden.';
        } else {
          this.mensajeError = 'Ha ocurrido un error. Inténtalo de nuevo.';
        }
      }
    });
  }
}