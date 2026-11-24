package edu.uclm.esi.tysweb.beusu.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellidos;
    private String correo;
    private String municipio;
    private Double latitud;
    private Double longitud;
    private String password;

    private String token;
    private LocalDateTime fechaCreacionToken;
    private LocalDateTime fechaValidacion;

    @Column(length = 36, unique = true)
    private String tokenSesion;
    private LocalDateTime horaTokenSesion;

    @Column(length = 36, unique = true)
    private String tokenRecuperacion;
    private LocalDateTime fechaTokenRecuperacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getFechaCreacionToken() { return fechaCreacionToken; }
    public void setFechaCreacionToken(LocalDateTime fechaCreacionToken) { this.fechaCreacionToken = fechaCreacionToken; }

    public LocalDateTime getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(LocalDateTime fechaValidacion) { this.fechaValidacion = fechaValidacion; }

    public boolean isActiva() { return fechaValidacion != null; }

    public String getTokenSesion() { return tokenSesion; }
    public void setTokenSesion(String tokenSesion) { this.tokenSesion = tokenSesion; }

    public LocalDateTime getHoraTokenSesion() { return horaTokenSesion; }
    public void setHoraTokenSesion(LocalDateTime horaTokenSesion) { this.horaTokenSesion = horaTokenSesion; }

    public String getTokenRecuperacion() { return tokenRecuperacion; }
    public void setTokenRecuperacion(String tokenRecuperacion) { this.tokenRecuperacion = tokenRecuperacion; }

    public LocalDateTime getFechaTokenRecuperacion() { return fechaTokenRecuperacion; }
    public void setFechaTokenRecuperacion(LocalDateTime fechaTokenRecuperacion) { this.fechaTokenRecuperacion = fechaTokenRecuperacion; }
}