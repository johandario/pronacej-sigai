package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PreguntaDTO implements Serializable {
    private long idPregunta;
    private String texto;
    private String categoria;
    private Integer orden;
    private Boolean requerido;
    private Boolean respuestasOrdenadas;
    private Boolean tieneObservaciones;
    private Boolean permiteDocumentos;
    private List<RespuestaDTO> respuestas = new ArrayList<>();
    private List<ContestacionDTO> contestaciones = new ArrayList<>();
}
