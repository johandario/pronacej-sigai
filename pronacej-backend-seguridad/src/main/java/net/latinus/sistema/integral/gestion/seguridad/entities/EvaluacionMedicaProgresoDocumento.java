package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "ia_evaluacion_medica_progreso_documento")
@EqualsAndHashCode(of = {"idEvaluacionMedicaProgresoDocumento"}, callSuper = true)
@Comment("Tabla que relaciona EvaluacionMedicaProgreso con Documentos dentro de Carpetas")
public class EvaluacionMedicaProgresoDocumento extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de la tabla")
    private Long idEvaluacionMedicaProgresoDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;

    @JoinColumn(name = "id_evaluacion_medica_progreso", referencedColumnName = "idEvaluacionMedicaProgreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la evaluación médica de progreso asociada")
    private EvaluacionMedicaProgreso evaluacionMedicaProgreso;

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
