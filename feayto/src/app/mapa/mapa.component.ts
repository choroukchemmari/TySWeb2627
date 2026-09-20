import {
  AfterViewInit,
  Component,
  OnDestroy
} from '@angular/core';

import * as L from 'leaflet';
import { DecimalPipe } from '@angular/common';
import { SimuladorService } from '../services/simulador.service';
import { VehiculoDto } from '../model/VehiculoDto';
import { FormsModule } from '@angular/forms';
import { MunicipioDto } from '../model/MunicipioDto';

@Component({
  selector: 'app-mapa',
  standalone: true,
  imports: [DecimalPipe, FormsModule],
  templateUrl: './mapa.component.html',
  styleUrl: './mapa.component.scss'
})
export class MapaComponent implements AfterViewInit, OnDestroy {

  cities: MunicipioDto[] = []
  selectedCity?: MunicipioDto
    private map!: L.Map;
  private markers = new Map<string, L.Marker>();
  private rutaSeleccionada?: L.Polyline;
  private refresco?: ReturnType<typeof setInterval>;

  vehiculos: VehiculoDto[] = [];

  constructor(private vehiculosService: SimuladorService) { }

  ngAfterViewInit(): void {
    this.map = L.map('map').setView(
      [38.984, -3.928],
      15
    );

    L.tileLayer(
      'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }
    ).addTo(this.map);
    this.vehiculosService.getCities().subscribe(
      cities => {
        this.cities = cities
      }
    )
  }

  loadMap(city: MunicipioDto) {
    this.selectedCity = city
    this.map.setView(
      [this.selectedCity.latitude, this.selectedCity.longitude],
      15
    );

    L.tileLayer(
      'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
      }
    ).addTo(this.map);

    setTimeout(() => {
      this.map.invalidateSize();
    });

    this.loadVehiculos()

    clearInterval(this.refresco)
    this.refresco = setInterval(() => this.loadVehiculos(), 3000)
  
  }

  mostrarRuta(vehiculo: VehiculoDto): void {
    if (!this.selectedCity)
      return

    this.vehiculosService
      .getRuta(this.selectedCity.name, vehiculo.matricula)
      .subscribe(puntos => {

        // Eliminar la ruta anterior
        if (this.rutaSeleccionada) {
          this.rutaSeleccionada.remove();
        }

        const latLngs: L.LatLngExpression[] =
          puntos.map(p => [
            p.latitude,
            p.longitude
          ]);

        this.rutaSeleccionada =
          L.polyline(latLngs, {
            weight: 4,
            opacity: 0.8
          });

        this.rutaSeleccionada.addTo(this.map);
      });
  }

  private crearBicycleIcon(matricula: string): L.DivIcon {
    return L.divIcon({
      className: 'bicycle-marker',
      html: `
      <div class="bicycle-marker-content">
        <div class="bicycle-icon">🚴🏼‍♀️</div>
        <div class="bicycle-label">${matricula}</div>
      </div>
    `,
      iconSize: [60, 48],
      iconAnchor: [30, 20]
    });
  }

  private actualizarMarcadores(): void {
    const matriculasActuales = new Set<string>();

    for (const vehiculo of this.vehiculos) {
      matriculasActuales.add(vehiculo.matricula);
      const marker = this.markers.get(vehiculo.matricula);
      if (marker) {
        marker.setLatLng([
          vehiculo.latitudActual,
          vehiculo.longitudActual
        ]);
      } else {
        const nuevoMarker = L.marker(
          [
            vehiculo.latitudActual,
            vehiculo.longitudActual
          ],
          {
            icon: this.crearBicycleIcon(vehiculo.matricula)
          }
        );

        nuevoMarker
          .bindTooltip(vehiculo.matricula)
          .addTo(this.map);

        this.markers.set(
          vehiculo.matricula,
          nuevoMarker
        );
      }
    }

    // Eliminar bicicletas que ya no existen
    for (const [matricula, marker] of this.markers) {
      if (!matriculasActuales.has(matricula)) {
        marker.remove();
        this.markers.delete(matricula);
      }
    }
  }

  simulacionAleatoria() {
    if (!this.selectedCity)
      return

    this.vehiculosService.simulacionAleatoria(this.selectedCity.name).subscribe(() => {
      this.loadVehiculos();
    });
  }

  generarBicicleta(): void {
    if (!this.selectedCity)
      return

    this.vehiculosService.generateBicycle(this.selectedCity.name).subscribe((ok) => {
      console.log(ok)
      this.loadVehiculos();
    },
    err => {
      console.error(err.error.error);
      alert(err.error.error);
    });
  }

  loadVehiculos(): void {
    if (!this.selectedCity)
      return

    this.vehiculosService.getVehiculos(this.selectedCity.name).subscribe(vehiculos => {
      this.vehiculos = vehiculos;
      this.actualizarMarcadores();
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.refresco)
    this.map?.remove();
  }
}