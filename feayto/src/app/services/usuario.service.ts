import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, map, switchMap, tap } from 'rxjs';
import { RegistroRequest } from '../model/RegistroRequest';
import { LoginRequest } from '../model/LoginRequest';
import { LoginResponse } from '../model/LoginResponse';
import { environment } from '../../environments/environment';

export interface Sesion {
  nombre: string;
  apellidos: string;
  correo: string;
  municipio: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private readonly urlUsuarios = `${environment.beusuUrl}/api/cuentas`;

  private http = inject(HttpClient);
  private router = inject(Router);

  // El usuario vive solo en memoria (nunca en sessionStorage)
  private usuarioSubject = new BehaviorSubject<Sesion | null>(null);
  public usuario$ = this.usuarioSubject.asObservable();

  // true mientras se valida el token con beusu (por ejemplo, tras pulsar F5)
  public isCheckingSession = false;

  constructor() {
    if (this.getSessionToken()) {
      this.isCheckingSession = true;
      this.autoLogin().subscribe({ error: () => { } });
    }
  }

  registrar(request: RegistroRequest) {
    return this.http.post(`${this.urlUsuarios}/registrar`, request);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.urlUsuarios}/login`, request).pipe(
      tap(respuesta => {
        this.setCookie('sessionToken', respuesta.token);
        this.setCookie('nombre', respuesta.nombre);
      }),
      switchMap(respuesta => this.autoLogin().pipe(map(() => respuesta)))
    );
  }

  autoLogin(): Observable<Sesion> {
    const token = this.getSessionToken() ?? '';
    return this.http.get<Sesion>(`${this.urlUsuarios}/validateSession`, {
      params: { sessionToken: token }
    }).pipe(
      tap({
        next: usuario => {
          this.isCheckingSession = false;
          this.usuarioSubject.next(usuario);
        },
        error: () => {
          this.isCheckingSession = false;
          this.limpiarSesion();
        }
      })
    );
  }

  logout() {
    const token = this.getSessionToken();
    const terminar = () => {
      this.limpiarSesion();
      this.router.navigate(['/login']);
    };

    if (!token) {
      terminar();
      return;
    }

    this.http.post(`${this.urlUsuarios}/logout`, null, {
      params: { sessionToken: token }
    }).subscribe({ next: terminar, error: terminar });
  }

  getUsuarioActual(): Sesion | null {
    return this.usuarioSubject.value;
  }

  getSessionToken(): string | null {
    return this.getCookie('sessionToken');
  }

  private limpiarSesion() {
    this.deleteCookie('sessionToken');
    this.deleteCookie('nombre');
    this.usuarioSubject.next(null);
  }

  private getCookie(nombre: string): string | null {
    const coincidencia = document.cookie.match(new RegExp('(?:^|; )' + nombre + '=([^;]*)'));
    return coincidencia ? decodeURIComponent(coincidencia[1]) : null;
  }

  private setCookie(nombre: string, valor: string, dias = 1) {
    document.cookie = `${nombre}=${encodeURIComponent(valor)}; path=/; max-age=${dias * 86400}; SameSite=Lax`;
  }

  private deleteCookie(nombre: string) {
    document.cookie = `${nombre}=; path=/; max-age=0`;
  }
}