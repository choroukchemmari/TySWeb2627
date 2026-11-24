import { Component, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { UsuarioService } from '../services/usuario.service';
import { MapaComponent } from '../mapa/mapa.component';
import { CrearVehiculosComponent } from '../crear-vehiculos/crear-vehiculos.component';
import { ComprarBicicletasComponent } from '../comprar-bicicletas/comprar-bicicletas.component';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [AsyncPipe, MapaComponent, CrearVehiculosComponent, ComprarBicicletasComponent],
  templateUrl: './menu.component.html'
})
export class MenuComponent {

  private usuarioService = inject(UsuarioService);

  usuario$ = this.usuarioService.usuario$;
  selectedOption = 0;

  select(option: number) {
    this.selectedOption = option;
  }

  cerrarSesion() {
    this.usuarioService.logout();
  }
}