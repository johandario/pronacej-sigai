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
@Table(name = "ia_plan_tratamiento_ind_carpeta")
@EqualsAndHashCode(of = {"idPlanTratamientoIndCarpeta"}, callSuper = true)
@Comment("Tabla de planes de tratamiento que se relacionan con carpetas")
public class PlanTratamientoIndCarpeta extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idPlanTratamientoIndCarpeta;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_plan_tratamiento", referencedColumnName = "idPlanTratamiento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del registro de pertenencias asociado")
    private PlanTratamientoInd planTratamientoInd;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
