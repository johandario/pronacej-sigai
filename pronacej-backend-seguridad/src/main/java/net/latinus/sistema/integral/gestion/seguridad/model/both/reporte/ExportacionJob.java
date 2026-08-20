package net.latinus.sistema.integral.gestion.seguridad.model.both.reporte;

import java.nio.file.Path;
import java.time.Instant;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.EstadoExportacionJob;

public class ExportacionJob {
    private final String id;
    private final Long idUsuarioSistema;
    private final Instant fechaCreacion = Instant.now();
    private final int totalAdolescentesSolicitados;
    private final int totalSeccionesSolicitadas;
    private volatile EstadoExportacionJob estado = EstadoExportacionJob.PENDIENTE;
    private volatile int loteActual = 0;
    private volatile int totalLotes = 0;
    private volatile int registrosProcesados = 0;
    private volatile Path rutaArchivo;
    private volatile String tokenDescarga;
    private volatile boolean tokenConsumido = false;
    private volatile String mensajeError;
    private volatile boolean cancelacionSolicitada = false;
    private volatile long tamanoOriginalBytes = 0L;

    public ExportacionJob(String id, Long idUsuarioSistema, int totalAdolescentesSolicitados, int totalSeccionesSolicitadas) {
        this.id = id;
        this.idUsuarioSistema = idUsuarioSistema;
        this.totalAdolescentesSolicitados = totalAdolescentesSolicitados;
        this.totalSeccionesSolicitadas = totalSeccionesSolicitadas;
    }

    public int getTotalAdolescentesSolicitados() {
        return this.totalAdolescentesSolicitados;
    }

    public int getTotalSeccionesSolicitadas() {
        return this.totalSeccionesSolicitadas;
    }

    public boolean isCancelacionSolicitada() {
        return this.cancelacionSolicitada;
    }

    public void setCancelacionSolicitada(boolean cancelacionSolicitada) {
        this.cancelacionSolicitada = cancelacionSolicitada;
    }

    public String getId() {
        return this.id;
    }

    public Long getIdUsuarioSistema() {
        return this.idUsuarioSistema;
    }

    public Instant getFechaCreacion() {
        return this.fechaCreacion;
    }

    public EstadoExportacionJob getEstado() {
        return this.estado;
    }

    public void setEstado(EstadoExportacionJob estado) {
        this.estado = estado;
    }

    public int getLoteActual() {
        return this.loteActual;
    }

    public void setLoteActual(int loteActual) {
        this.loteActual = loteActual;
    }

    public int getTotalLotes() {
        return this.totalLotes;
    }

    public void setTotalLotes(int totalLotes) {
        this.totalLotes = totalLotes;
    }

    public int getRegistrosProcesados() {
        return this.registrosProcesados;
    }

    public void setRegistrosProcesados(int registrosProcesados) {
        this.registrosProcesados = registrosProcesados;
    }

    public Path getRutaArchivo() {
        return this.rutaArchivo;
    }

    public void setRutaArchivo(Path rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public String getTokenDescarga() {
        return this.tokenDescarga;
    }

    public void setTokenDescarga(String tokenDescarga) {
        this.tokenDescarga = tokenDescarga;
    }

    public boolean isTokenConsumido() {
        return this.tokenConsumido;
    }

    public void setTokenConsumido(boolean tokenConsumido) {
        this.tokenConsumido = tokenConsumido;
    }

    public String getMensajeError() {
        return this.mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public long getTamanoOriginalBytes() {
        return this.tamanoOriginalBytes;
    }

    public void setTamanoOriginalBytes(long tamanoOriginalBytes) {
        this.tamanoOriginalBytes = tamanoOriginalBytes;
    }
}
