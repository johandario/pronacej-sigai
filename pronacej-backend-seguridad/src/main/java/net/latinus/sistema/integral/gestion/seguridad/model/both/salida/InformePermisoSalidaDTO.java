package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPermisoSalida"}, callSuper = true)
public class InformePermisoSalidaDTO extends CamposDTO{
    private Long idPermisoSalida;
    private Long tokenFichaIdentificacion; // Token identificador de la Ficha Identificación
    private CatalogoDTO motivoSalida;
    private Date fechaHoraSalida;
    private Date fechaHoraRegreso;
    private String usuarioSalida;
    private String nroDocumento;
    private String observaciones;
    private CatalogoDTO tipoSalida;
    private CatalogoDTO frecuenciaSalida;
    private String tipoSalidaLugar;
    private List<ActividadSalidaDTO> actividades;
    private String nombreAdolescente;
    private String tokenIdentificadorAdolescente; // Token identificador del adolescente
    private String dniAdolescente; // DNI del adolescente
    private String nombreFrecuenciSalida;
    private String nombreTipoSalida;
    private Boolean isComplete;
    private CatalogoDTO estadoEvento;
    private JerarquiaDTO centro;
    private String otrosSalida;

    public String getNombreAdolescente() {
        return nombreAdolescente;
    }

    public void setNombreAdolescente(String nombreAdolescente) {
        this.nombreAdolescente = nombreAdolescente;
    }

    public String getDniAdolescente() {
        return dniAdolescente;
    }

    public void setDniAdolescente(String dniAdolescente) {
        this.nombreAdolescente = dniAdolescente;
    }

    public String getTokenAdolescente() {
        return tokenIdentificadorAdolescente;
    }

    public void setTokenAdolescente(String tokenIdentificadorAdolescente) {
        this.nombreAdolescente = tokenIdentificadorAdolescente;
    }

    public String getNombreFrecuenciaSalida() {
        return nombreFrecuenciSalida;
    }

    public void setNombreFrecuenciaSalida(String nombreFrecuenciSalida) {
        this.nombreFrecuenciSalida = nombreFrecuenciSalida;
    }

    public String getNombreTipoSalida() {
        return nombreTipoSalida;
    }

    public void setNombreTipoSalida(String nombreTipoSalida) {
        this.nombreTipoSalida = nombreTipoSalida;
    }
}
