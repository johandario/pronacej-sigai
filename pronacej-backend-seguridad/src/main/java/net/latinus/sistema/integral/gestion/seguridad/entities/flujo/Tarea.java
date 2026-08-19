package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.List;

@Entity
@Data
@Table(name = "flu_tarea")
@Comment("Tabla de gestión de tareas basadas en los pasos")
@EqualsAndHashCode(of = {"idTarea"}, callSuper = true)
public class Tarea extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idTarea;

    @Comment("Estado de la tarea: Pendiente, En curso, Completada, Rechazada")
    private String estado;

    @Comment("Comentario general")
    private String comentario;

    @Comment("Comentario referente a rechazo")
    private String comentarioRechazo;

    @Comment("Ruta del componente de la tarea")
    private String url;

    @Comment("El número secuencial del orden de la serie de pasos")
    private Integer orden;

    @Comment("Rol o Usuario que tiene acceso a dicha tarea")
    private String rolUsuarioEnvia;

    @Comment("Rol o Usuario que tiene acceso a dicha tarea")
    private String rolUsuarioRecibe;

    @Comment("Nombre del proceso a que pertenece")
    private String nombreProceso;

    @OneToMany(mappedBy = "tarea", fetch = FetchType.LAZY)
    @Comment("Lista de usuarios con acceso a la tarea")
    private List<TareaUsuario> tareaUsuarioList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paso")
    @JsonIgnore
    @Comment("Paso al que pertenece")
    private Paso paso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variable_proceso")
    @Comment("Valor de variable en caso de que sea condicional")
    private VariableProceso variableProceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_instancia_proceso")
    @Comment("Instancia al que pertenece")
    private InstanciaProceso instanciaProceso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
