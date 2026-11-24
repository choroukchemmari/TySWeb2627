import { Component, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComprasService, Prerreserva } from '../services/compras.service';
import { environment } from '../../environments/environment';

// Stripe.js se carga con un <script> en index.html
declare let Stripe: any;

@Component({
  selector: 'app-comprar-bicicletas',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './comprar-bicicletas.component.html'
})
export class ComprarBicicletasComponent {

  private compras = inject(ComprasService);

  cantidad = 1;
  prerreserva?: Prerreserva;
  mensaje = '';
  esError = false;
  cargando = false;
  pagando = false;

  private idTransaccion?: string;
  private stripe: any;
  private card: any;

  // Pasos 1 y 2: prerreserva en bealquileres y prepago en beusu
  solicitar() {
    this.mensaje = '';
    this.cargando = true;
    this.compras.prerreservar(this.cantidad).subscribe({
      next: prerreserva => {
        this.compras.prepay(prerreserva.reservaId).subscribe({
          next: respuesta => {
            this.prerreserva = prerreserva;
            this.idTransaccion = respuesta.idTransaccion;
            this.cargando = false;
            this.mostrarFormulario();
          },
          error: error => this.fallo(error)
        });
      },
      error: error => this.fallo(error)
    });
  }

  // Paso 3: formulario de tarjeta de Stripe (los datos van directamente a Stripe, no a nuestros servidores)
  private mostrarFormulario() {
    // Se espera a que Angular pinte el <div id="card-element"> antes de montar el formulario
    setTimeout(() => {
      this.stripe = Stripe(environment.stripePublicKey);
      this.card = this.stripe.elements().create('card');
      this.card.mount('#card-element');
    });
  }

  pagar() {
    if (!this.stripe || !this.idTransaccion)
      return;
    this.pagando = true;
    this.stripe.confirmCardPayment(this.idTransaccion, { payment_method: { card: this.card } })
      .then((respuesta: any) => {
        if (respuesta.error) {
          this.mostrar(respuesta.error.message, true);
          this.pagando = false;
          return;
        }
        if (respuesta.paymentIntent.status === 'succeeded') {
          // Paso 4: se lo contamos a beusu, que lo comprobará con Stripe
          this.compras.confirm(this.idTransaccion!).subscribe({
            next: () => {
              this.limpiarFormulario();
              this.mostrar('Compra realizada: las bicicletas ya son de su municipio.', false);
            },
            error: error => this.fallo(error)
          });
        }
      });
  }

  cancelar() {
    // La prerreserva caduca sola a los 30 minutos y las bicicletas vuelven a estar libres
    this.limpiarFormulario();
    this.mensaje = '';
  }

  private limpiarFormulario() {
    this.card?.destroy();
    this.card = undefined;
    this.prerreserva = undefined;
    this.idTransaccion = undefined;
    this.pagando = false;
    this.cargando = false;
  }

  private fallo(error: any) {
    this.cargando = false;
    this.pagando = false;
    this.mostrar(error?.error?.message ?? 'No se ha podido completar la operación', true);
  }

  private mostrar(mensaje: string, esError: boolean) {
    this.mensaje = mensaje;
    this.esError = esError;
  }
}