package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExpedienteMatrizDetalleDTO extends CamposDTO implements Serializable {
    private Long idExpedienteDetalle;
    private CatalogoDTO tipoRegistro;
    private CatalogoDTO estado;
    private CatalogoDTO situacionJuridica;
    private CatalogoDTO variacionMedida;
    private CatalogoDTO tipoVariacion;
    private CatalogoDTO motivoVariacion;
    private String numResolucion;
    private Date fechaResolucion;
    private String decision;
    private Integer tiempoMedSocEduAnios;
    private Integer tiempoMedSocEduMeses;
    private Integer tiempoMedSocEduDias;
    private Date fechaInicioMedida;
    private Date fechaFinMedida;
    private CatalogoDTO corteJusticia;
    private CatalogoDTO instancia;
    private CatalogoDTO especialidad;
    private String organoJurisdiccional;
    private String juez;
    private String secretario;
    private CatalogoDTO sancionImpuesta;
    private BigDecimal montoReparacion;
    private CatalogoDTO tipoMedSocEduImp;
    private String lugarInfraccion;
    private Integer numJornadas;
    private CatalogoDTO frecuenciaIngreso;
    private String numExpediente;
    private List<ExpedienteMatrizDelitoDTO> expedienteDelitos;
    private List<ExpedienteMatrizMedidaDTO> medidasSocioeducativas;
    private List<ExpedienteMatrizMedidaDTO> medidasAccesorias;
    private String numExpedienteJudicial;

    private Boolean removido;
}
