import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { UsuarioService } from '../services/usuario.service';
import { RegistroRequest } from '../model/RegistroRequest';

function passwordsIgualesValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmacionPassword = control.get('confirmacionPassword')?.value;
  return password === confirmacionPassword ? null : { passwordsDistintas: true };
}

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.scss'
})
export class RegistroComponent {

  form: FormGroup;

  constructor(private fb: FormBuilder, private service: UsuarioService) {
    this.form = this.fb.group({
      nombre: ['', Validators.required],
      apellidos: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      municipio: ['', Validators.required],
      latitud: [null, Validators.required],
      longitud: [null, Validators.required],
      password: ['', Validators.required],
      confirmacionPassword: ['', Validators.required]
    }, { validators: passwordsIgualesValidator });
  }

  registrar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request: RegistroRequest = this.form.value;
    this.service.registrar(request).subscribe(
      ok => {
        alert('Registro correcto. Revisa tu correo para confirmar la cuenta.');
        this.form.reset();
      },
      error => {
        alert(error.error.message ?? 'Error al registrar');
      }
    );
  }
}