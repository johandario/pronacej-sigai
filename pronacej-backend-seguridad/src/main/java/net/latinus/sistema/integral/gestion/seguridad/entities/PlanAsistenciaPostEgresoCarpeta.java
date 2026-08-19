package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_plan_asistencia_post_egreso_carpeta")
@EqualsAndHashCode(of = {"idPlanAsistenciaPostEgresoCarpeta"}, callSuper = true)
@Comment("Tabla de plan asistencia post egreso que se relacionan con carpetas")
public class PlanAsistenciaPostEgresoCarpeta extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idPlanAsistenciaPostEgresoCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_plan_asistencia_post_egreso", referencedColumnName = "idPlanAsistenciaPostEgreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro dl plan de asistencia asociado")
    private PlanAsistenciaPostEgreso planAsistenciaPostEgreso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
