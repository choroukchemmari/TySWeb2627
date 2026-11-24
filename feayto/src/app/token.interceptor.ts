import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../environments/environment';

// Lee la cookie directamente (no usamos UsuarioService para evitar una dependencia circular con HttpClient)
function leerCookie(nombre: string): string | null {
  const coincidencia = document.cookie.match(new RegExp('(?:^|; )' + nombre + '=([^;]*)'));
  return coincidencia ? decodeURIComponent(coincidencia[1]) : null;
}

/**
 * Añade el token de sesión (X-Session-Token) a las peticiones que lo necesitan:
 * las de bealquileres y las de pagos de beusu. El resto (login, registro...) no lo llevan.
 */
export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token = leerCookie('sessionToken');
  const necesitaToken =
    req.url.startsWith(environment.bealquileresUrl) ||
    req.url.startsWith(`${environment.beusuUrl}/api/payments`);

  if (token && necesitaToken) {
    req = req.clone({ setHeaders: { 'X-Session-Token': token } });
  }
  return next(req);
};