package edu.uclm.esi.tysweb.beusu.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Compra {
    @Id @Column(length = 36)
    private String id;

    // client_secret de Stripe: identifica la transacción (idTransaccion)
    @Column(unique = true, nullable = false)
    private String idTransaccion;

    // Una prerreserva de bealquileres solo puede tener una compra
    @Column(unique = true, nullable = false, length = 36)
    private String reservaId;

    // Correo del funcionario que compra
    @Column(nullable = false)
    private String correo;

    private int cantidad;

    // Importe en euros
    private int importe;

    private LocalDateTime fecha = LocalDateTime.now();

    // null mientras no se haya confirmado el pago
    private LocalDateTime fechaConfirmacion;

    public Compra() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(String idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getImporte() {
        return importe;
    }

    public void setImporte(int importe) {
        this.importe = importe;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) {
        this.fechaConfirmacion = fechaConfirmacion;
    }

    public boolean isConfirmada() {
        return fechaConfirmacion != null;
    }
}