package net.latinus.sistema.integral.gestion.seguridad.model.both.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DocumentoDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificacionDTO extends CamposDTO {

    private Long idNotificacion;

    private String remitente;

    private String destinatarios;

    private String cuerpo;

    private String asunto;

    private String tipo;

    private String medio;

    private Long adolescente;

    private String observacionesEntrega;

    private String entregado;

    private Date fechaEntrega;

    private List<DocumentoDTO> documentoDTOList;

    private String tokenFichaIdentificacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
