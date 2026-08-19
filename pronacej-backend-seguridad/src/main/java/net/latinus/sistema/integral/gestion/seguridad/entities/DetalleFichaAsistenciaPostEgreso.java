package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "seg_detalle_ficha_asistencia_post_egreso")
@EqualsAndHashCode(of = {"idDetalleFichaAsistenciaPostEgreso"}, callSuper = true)
@Comment("Tabla detalle de ficha de asistencia post egreso")
public class DetalleFichaAsistenciaPostEgreso extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal del detalle de ficha de asistencia post egreso")
    private Long idDetalleFichaAsistenciaPostEgreso;

    @ManyToOne
    @JoinColumn(name = "id_ficha_asistencia_post_egreso", referencedColumnName = "idFichaAsistenciaPostEgreso")
    @Comment("Ficha de asistencia post egreso padre")
    private FichaAsistenciaPostEgreso fichaAsistenciaPostEgreso;

    @Comment("Fecha del detalle")
    private Date fechaDetalle;

    @Comment("Descripción de la actividad")
    @Column(columnDefinition = "TEXT")
    private String descripcionActividad;

    @Comment("Observaciones")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_modalidad_entrevista")
    @Comment("Modalidad de la entrevista")
    private Catalogo modalidadDeEntrevista;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_persona_entrevistada")
    @Comment("Persona entrevistada")
    private Catalogo personaEntrevistada;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_motivo")
    @Comment("Motivo de la asistencia post egreso")
    private Catalogo motivo;

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            LogService logService = new LogService(ex.getClass());
            logService.error("Ha ocurrido un error: {}", ex.getMessage(), ex);
            return "";
        }
    }

}
