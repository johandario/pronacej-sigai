package net.latinus.sistema.integral.gestion.seguridad.model.both.flujo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idTarea"}, callSuper = true)
public class TareaDTO extends CamposDTO implements Serializable {
    private Long idTarea;
    private String estado;
    private String comentario;
    private String comentarioRechazo;
    private String url;
    private PasoDTO paso;
    private Integer orden;
    private String rolUsuarioEnvia;
    private String rolUsuarioRecibe;
    private String nombreProceso;
    private Date fechaEdicion;
    private String tipo;
    private String descripcion;

}
