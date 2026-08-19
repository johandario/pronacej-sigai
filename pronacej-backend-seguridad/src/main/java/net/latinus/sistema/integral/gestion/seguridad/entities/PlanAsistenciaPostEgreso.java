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
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "seg_plan_asistencia_post_egreso")
@Comment("Encabezado de Plan de Asistencia Post Egreso")
@EqualsAndHashCode(of = {"idPlanAsistenciaPostEgreso"}, callSuper = true)
public class PlanAsistenciaPostEgreso extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idPlanAsistenciaPostEgreso;

    @Comment("Estado del plan")
    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado")
    private Catalogo estado;

    @Comment("Fecha de inicio de plan")
    private Date fechaInicio;

    @Comment("Fecha de finalización de plan")
    private Date fechaFin;

    @OneToMany(mappedBy = "planAsistencia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Comment("Detalle de plan asistencia post egreso")
    private List<PlanAsistenciaPostEgresoDetalle> planDetalle;
    
    @ManyToOne
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación padre")
    private FichaIdentificacion fichaIdentificacion;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Empresa a la que pertenence el expediente")
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
