package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "ia_plan_tratamiento_ind_segui_abierto")
@Comment("Ficha de seguimiento de Plan de tratamiento individual regimen abierto")
@EqualsAndHashCode(of = {"idPlanTratamientoIndSeguiAbierto"}, callSuper = true)
public class PlanTratamientoIndSeguiAbierto extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanTratamientoIndSeguiAbierto;

    @Comment("Fecha de seguimiento")
    private Date fecha;

    @Comment("Hora de seguimiento")
    private String hora;

    @Comment("Descripción de seguimiento")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_tratamiento_ind_interv")
    @Comment("Intervención de pti al que se encuentra relacionado")
    private PlanTratamientoIndInterv planTratamientoIndInterv;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
