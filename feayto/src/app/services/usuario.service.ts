import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RegistroRequest } from '../model/RegistroRequest';
import { LoginRequest } from '../model/LoginRequest';
import { LoginResponse } from '../model/LoginResponse';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private readonly urlUsuarios = `${environment.beusuUrl}/api/cuentas`;

  private http = inject(HttpClient);

  registrar(request: RegistroRequest) {
    return this.http.post(`${this.urlUsuarios}/registrar`, request);
  }

  login(request: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.urlUsuarios}/login`, request);
  }
}