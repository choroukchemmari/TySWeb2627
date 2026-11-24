import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

export interface Prerreserva {
  reservaId: string;
  municipio: string;
  cantidad: number;
  importe: number;
  expira: string;
}

@Injectable({
  providedIn: 'root'
})
export class ComprasService {

  private http = inject(HttpClient);

  // 1. bealquileres preasigna las bicicletas durante 30 minutos
  prerreservar(cantidad: number) {
    return this.http.post<Prerreserva>(`${environment.bealquileresUrl}/compras/prerreservar`, { cantidad });
  }

  // 2. beusu pide el pago a Stripe y devuelve el client_secret (idTransaccion)
  prepay(reservaId: string) {
    return this.http.post<{ idTransaccion: string }>(`${environment.beusuUrl}/api/payments/prepay`, null, {
      params: { reservaId }
    });
  }

  // 4. Tras pagar, beusu comprueba con Stripe que el pago es real y confirma la compra
  confirm(idTransaccion: string) {
    return this.http.post<void>(`${environment.beusuUrl}/api/payments/confirm`, { idTransaccion });
  }
}