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
@Table(name = "flu_instancia_proceso")
@Comment("Encabezado de registros que incorporan un proceso")
@EqualsAndHashCode(of = {"idInstanciaProceso"}, callSuper = true)
public class InstanciaProceso extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idInstanciaProceso;

    @Comment("Estado de la instancia")
    private String estado;

    @Comment("Descripción")
    private String descripcion;

    @OneToMany(mappedBy = "instanciaProceso", fetch = FetchType.LAZY)
    @Comment("Lista de tareas")
    private List<Tarea> tareas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso")
    @JsonIgnore
    @Comment("Proceso de la instancia")
    private Proceso proceso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
