package net.latinus.sistema.integral.gestion.seguridad.model.both.flujo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.PasoRol;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.PasoUsuario;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPaso"},callSuper = true)
public class PasoDTO extends CamposDTO implements Serializable {
    private Long idPaso;
    private String nombre;
    private String url;
    private Integer porcentajeAvance;
    private Integer orden;
    private PasoDTO pasoAnterior;
    private PasoDTO pasoSiguiente;
    private PasoDTO pasoSubsanacion;
    private String jsonCondicional;
    private String rolUsuario;
    private Boolean requiereNotificacionCorreo;
    private Boolean omitePaso;
    private String rolUsuarioNotificacion;
    private Boolean removido;
    private List<PasoUsuarioDTO> pasoUsuarioList;
    private List<PasoRolDTO> pasoRolList;
    private String icono;
    private Integer pasoSalto;
}
