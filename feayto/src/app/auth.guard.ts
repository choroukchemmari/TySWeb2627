import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { filter, map, take } from 'rxjs';
import { UsuarioService } from './services/usuario.service';

export const authGuard: CanActivateFn = () => {
  const usuarioService = inject(UsuarioService);
  const router = inject(Router);

  // 1. Ya hay usuario validado en memoria: adelante
  if (usuarioService.getUsuarioActual()) {
    return true;
  }

  // 2. Hay un token y beusu lo está validando (por ejemplo, tras F5): esperar la respuesta
  if (usuarioService.isCheckingSession) {
    return usuarioService.usuario$.pipe(
      filter(() => !usuarioService.isCheckingSession),
      take(1),
      map(usuario => usuario ? true : router.createUrlTree(['/login']))
    );
  }

  // 3. Sin sesión ni validación pendiente: a /login
  return router.createUrlTree(['/login']);
};