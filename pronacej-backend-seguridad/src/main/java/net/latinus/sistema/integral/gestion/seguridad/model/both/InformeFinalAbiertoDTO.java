package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class InformeFinalAbiertoDTO extends CamposDTO implements Serializable {
    private Long idInformeFinalAbierto;
    private String fortalecimientoDerechos;
    private String area;
    private String fortalecimientoFamiliar;
    private String intervencion;
    private String enfoque;
    private String cultural;
    private String responsabilidad;
    private String conciencia;
    private Date fechaFinalizacion;
    private Boolean completado;
    private List<InformeFinalAbiertoMedidasDTO> medidasList;
    private String valoracionRiesgo;
    private String conclusionesRecomendaciones;
    private String tokenFichaIdenticacion;
    private Long idFichaIdentificacion;
    private List<DocumentoDTO> documentoDTOList;
}
