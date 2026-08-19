package net.latinus.sistema.integral.gestion.seguridad.entities.salida;

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

@Entity
@Data
@Table(name = "act_salida")
@Comment("Tabla que registra las actividades realizadas en la salida ")
@EqualsAndHashCode(of = {"idActividadSalida"}, callSuper = true)
public class ActividadSalida extends EntidadBase{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idActividadSalida;


    @ManyToOne
    @JoinColumn(name = "id_permiso_salida")
    @Comment("Salida a la que pertenece")
    private InformePermisoSalidaAdolescente informePermisoSalidaAdolescente;

    @Comment("Descripcion de la actividad de salida")
    private String descripcion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
