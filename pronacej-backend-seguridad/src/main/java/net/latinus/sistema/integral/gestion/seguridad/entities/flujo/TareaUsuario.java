package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "flu_tarea_usuario")
@Comment("Tabla de relación entre una tarea y un usuario")
@EqualsAndHashCode(of = {"idTareaUsuario"}, callSuper = true)
public class TareaUsuario extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idTareaUsuario;

    @ManyToOne
    @JoinColumn(name = "id_tarea", nullable = false)
    @Comment("Paso del proceso respectivo")
    private Tarea tarea;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @Comment("Usuario con permiso a ese paso")
    private UsuarioSistema usuarioSistema;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
