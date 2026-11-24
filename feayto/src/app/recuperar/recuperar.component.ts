import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UsuarioService } from '../services/usuario.service';

@Component({
  selector: 'app-recuperar',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './recuperar.component.html'
})
export class RecuperarComponent {

  private usuarioService = inject(UsuarioService);
  private fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  cargando = false;
  enviado = false;

  solicitar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando = true;
    const { email } = this.form.getRawValue();

    // Mensaje neutro siempre: no se revela si el correo existe (RC-SEC-03)
    this.usuarioService.solicitarNuevaPassword(email).subscribe({
      next: () => this.terminar(),
      error: () => this.terminar()
    });
  }

  private terminar() {
    this.cargando = false;
    this.enviado = true;
  }
}