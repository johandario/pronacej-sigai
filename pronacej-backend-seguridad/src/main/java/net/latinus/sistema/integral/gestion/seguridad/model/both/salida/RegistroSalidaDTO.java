package net.latinus.sistema.integral.gestion.seguridad.model.both.salida;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.InformeFinalAbiertoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;


import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idRegistroSalida"}, callSuper = true)
public class RegistroSalidaDTO extends CamposDTO{
    private Long idRegistroSalida;
    private Long tokenFichaIdentificacion;
    private CatalogoDTO motivoSalida;
    private Date fechaHoraSalida;
    private Date fechaHoraRegreso;
    private String usuarioSalida;
    private String nroDocumento;
    private String observaciones;
    private JerarquiaDTO centroSalida;
    private CatalogoDTO tipoSalida;
    private String tipoSalidaLugar;
    private String nombreAdolescente;
    private String tokenIdentificadorAdolescente;
    private String dniAdolescente;
    private String nombreMotivoSalida;
    private String nombreTipoSalida;
    private EventoFugaDTO eventoFuga;
    private TrasladoDTO traslado;
    private InformePermisoSalidaDTO permisoSalida;
    private ActaExternamientoDTO externamiento;
    private InformeFinalAbiertoDTO informeFinalAbierto;





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

    public String getnombreMotivoSalida() {
        return nombreMotivoSalida;
    }

    public void setNombreMotivoSalida(String nombreMotivoSalida) {
        this.nombreMotivoSalida = nombreMotivoSalida;
    }
}
