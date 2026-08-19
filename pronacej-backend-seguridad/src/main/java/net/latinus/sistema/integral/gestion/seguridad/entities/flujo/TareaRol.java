package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "flu_tarea_rol")
@Comment("Tabla de relación entre una tarea y un rol")
@EqualsAndHashCode(of = {"idTareaRol"}, callSuper = true)
public class TareaRol extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idTareaRol;

    @ManyToOne
    @JoinColumn(name = "id_tarea", nullable = false)
    @Comment("Paso del proceso respectivo")
    private Tarea tarea;

    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    @Comment("Rol con permiso a ese paso")
    private Rol rol;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
