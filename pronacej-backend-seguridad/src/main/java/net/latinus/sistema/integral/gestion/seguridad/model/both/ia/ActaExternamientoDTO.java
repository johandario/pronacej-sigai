package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActaExternamientoDTO extends CamposDTO {
    private Long idActaExternamiento;
    private Date fechaRegistro;
    private String ingreso;
    private String institucion;
    private String autorizacion;
    private String tipoDocumento;
    private String nemonicoTipoDocumento;
    private String numeroDocumento;
    private String resolucion;
    private String domicilio;
    private Boolean mandatoDetencion;
    private Boolean retiroSolo;
    private Boolean impreso;
    private Boolean firmado;
    private String familiares;
    private String parentescos;
    private String identificaciones;
    private String direcciones;
    private String telefonos;
    private String observaciones;
    private String tokenFichaIdentificacion;
    private String tokenExpedienteMatriz;
    private String numeroExpedienteMatriz;
    private ActaExternamientoDocumentoDTO actaExternamientoDocumentoDTO;
    private CatalogoDTO estadoEvento;
    private Boolean isComplete;
}
