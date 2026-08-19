package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_evaluacion_seguimiento_educativo_documento")
@EqualsAndHashCode(of = {"idEvaluacionSeguimientoEducativoDocumento"}, callSuper = true)
@Comment("Tabla de seguimiento educativo laboral que relaciona con los documentos")
public class EvaluacionSeguimientoEducativoDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idEvaluacionSeguimientoEducativoDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_evaluacion_seguimiento", referencedColumnName = "idEvaluacionSeguimiento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del seguimiento al que esta asociado")
    private EvaluacionSeguimientoEducativoLaboral evaluacionSeguimientoEducativoLaboral;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
