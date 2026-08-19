package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SeccionDTO implements Serializable {
    private long idSeccion;
    private String nombre;
    private Integer orden;
    private Boolean preguntasOrdenadas;
    private Boolean tienePuntuacion;
    private List<PreguntaDTO> preguntas = new ArrayList<>();
}
