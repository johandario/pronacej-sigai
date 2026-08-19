package net.latinus.sistema.integral.gestion.seguridad.entities.flujo;

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
@Table(name = "flu_proceso")
@Comment("Tabla para gestionar diferentes procesos a definir")
@EqualsAndHashCode(of = {"idProceso"},callSuper = true)
public class Proceso extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idProceso;

    @Comment("Nombre del proceso")
    private String nombre;

    @Comment("Version del proceso")
    private Integer version;

    @Comment("Nemonico del proceso")
    private String nemonico;

    @OneToMany(mappedBy = "proceso", fetch = FetchType.LAZY)
    @Comment("Primer paso del proceso")
    private List<Paso> pasos;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
