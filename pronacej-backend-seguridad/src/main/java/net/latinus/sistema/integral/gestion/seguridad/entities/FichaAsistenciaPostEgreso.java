package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import java.util.List;

@Entity
@Data
@Table(name = "seg_ficha_asistencia_post_egreso")
@Comment("ficha de asistencia")
@EqualsAndHashCode(of = {"idFichaAsistenciaPostEgreso"}, callSuper = true)
public class FichaAsistenciaPostEgreso extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la ficha de asistencia post egreso")
    private Long idFichaAsistenciaPostEgreso;

    @Comment("Tipo Formato")
    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado")
    private Catalogo tipoFormato;

    @ManyToOne
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación padre")
    private FichaIdentificacion fichaIdentificacion;

    @OneToMany(mappedBy = "fichaAsistenciaPostEgreso", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("Detalle de ficha")
    private List<DetalleFichaAsistenciaPostEgreso> detalleFichaAsistenciaPostEgresos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_asistencia_post_egreso_detalle")
    @Comment("Detalle del plan de asistencia post egreso relacionado")
    private PlanAsistenciaPostEgresoDetalle planAsistenciaPostEgresoDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan_asistencia_post_egreso")
    @Comment("Detalle del plan de asistencia post egreso relacionado")
    private PlanAsistenciaPostEgreso planAsistenciaPostEgreso;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
