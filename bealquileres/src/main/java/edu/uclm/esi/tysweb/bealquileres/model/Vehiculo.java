package edu.uclm.esi.tysweb.bealquileres.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity 
public class Vehiculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitud;
    private Double longitud;

    private Double distanciaTotal = 0.0;
    private Double bateria = 100.0;

    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "municipio_id", nullable = true)
    private Municipio municipio;

    // Estación de recarga en la que está la bicicleta (null si no está en ninguna)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "estacion_id", nullable = true)
    private Estacion estacion;

    // Estado de la bicicleta y observaciones del funcionario (pinchada, robada...)
    @Enumerated(EnumType.STRING)
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Column(length = 500)
    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getDistanciaTotal() {
        return distanciaTotal;
    }

    public void setDistanciaTotal(Double distanciaTotal) {
        this.distanciaTotal = distanciaTotal;
    }

    public Double getBateria() {
        return bateria;
    }

    public void setBateria(Double bateria) {
        this.bateria = bateria;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getMatricula() {
        return String.format("%04d", this.id);
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Estacion getEstacion() {
        return estacion;
    }

    public void setEstacion(Estacion estacion) {
        this.estacion = estacion;
    }

    public EstadoVehiculo getEstado() {
        // Las bicicletas creadas antes de existir este campo no tienen estado guardado
        return this.estado != null ? this.estado : EstadoVehiculo.DISPONIBLE;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}