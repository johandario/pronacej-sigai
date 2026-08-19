package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_plan_asistencia_post_egreso_detalle")
@Comment("Detalle de Plan de Asistencia Post Egreso")
@EqualsAndHashCode(of = {"idPlanAsistenciaPostEgresoDetalle"}, callSuper = true)
public class PlanAsistenciaPostEgresoDetalle extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanAsistenciaPostEgresoDetalle;

    @Comment("Áreas de plan")
    @ManyToOne
    @JoinColumn(name = "id_catalogo_area")
    private Catalogo area;

    @Comment("Factores de riesgo o protectores")
    @Column(columnDefinition = "TEXT")
    private String factores;

    @Comment("Objetivo General")
    @Column(columnDefinition = "TEXT")
    private String objetivoGeneral;

    @Comment("Objetivo específico")
    @Column(columnDefinition = "TEXT")
    private String objetivoEspecifico;

    @Comment("Actividades")
    @Column(columnDefinition = "TEXT")
    private String actividades;

    @Comment("Institución donde participa")
    @Column(columnDefinition = "TEXT")
    private String institucion;

    @Comment("Frecuencia o tiempo de seguimiento")
    @Column(columnDefinition = "TEXT")
    private String frecuencia;

    @Comment("Registro o indicador de seguimiento")
    @Column(columnDefinition = "TEXT")
    private String indicador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_asistencia_post_egreso")
    @Comment("Encabezado al que pertence el plan")
    private PlanAsistenciaPostEgreso planAsistencia;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Empresa a la que pertenence el expediente")
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
