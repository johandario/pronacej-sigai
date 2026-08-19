package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.Data;
import java.io.Serializable;

@Data
public class RespuestaDTO implements Serializable {
    private long idRespuesta;
    private String respuesta;
    private long valorRespuesta;
    private Integer orden;
    private Boolean respuestaCorrecta;
}
