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

@Entity
@Data
@Table(name = "seg_informe_final_asistencia_detalle")
@Comment("Detalle de Informe Final de Asistencia")
@EqualsAndHashCode(of = {"idInformeFinalAsistenciaDetalle"}, callSuper = true)
public class InformeFinalAsistenciaDetalle extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idInformeFinalAsistenciaDetalle;

    @Comment("Área")
    @ManyToOne
    @JoinColumn(name = "id_catalogo_area")
    private Catalogo area;

    @Comment("Objetivo General")
    @Column(columnDefinition = "TEXT")
    private String objetivoGeneral;

    @Comment("Objetivo específico")
    @Column(columnDefinition = "TEXT")
    private String objetivoEspecifico;

    @Comment("Actividades")
    @Column(columnDefinition = "TEXT")
    private String actividades;

    @Comment("Descripción de las actividades realizadas")
    @Column(columnDefinition = "TEXT")
    private String descripcionActividad;

    @Comment("Logros")
    @Column(columnDefinition = "TEXT")
    private String logro;

    @Comment("Dificultades")
    @Column(columnDefinition = "TEXT")
    private String dificultad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_informe_final_asistencia")
    @Comment("Encabezado al que pertence el plan")
    private InformeFinalAsistencia informeFinalAsistencia;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
