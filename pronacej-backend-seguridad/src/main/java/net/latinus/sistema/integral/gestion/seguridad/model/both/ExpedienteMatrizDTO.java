package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteMatrizDTO extends CamposDTO implements Serializable {
    private Long idExpediente;
    private String numExpediente;
    private CatalogoDTO estado;
    private String numOficio;
    private Date fechaOficio;
    private String fecOficioTexto;
    private String observacion;
    private String tipoCentro;
    private String motivoIngreso;
    private List<ExpedienteMatrizDetalleDTO> expedienteDetalle;

    private String tokenFichaIdentificacion;
    private String tokenFichaIngreso;
    private String numExpedienteJudicial;
}
