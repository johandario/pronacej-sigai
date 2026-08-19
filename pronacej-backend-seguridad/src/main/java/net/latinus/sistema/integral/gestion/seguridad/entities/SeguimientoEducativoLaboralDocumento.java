package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_seguimiento_educativo_laboral_documento")
@EqualsAndHashCode(of = {"idSeguimientoEducativoLaboralDocumento"}, callSuper = true)
@Comment("Tabla de seguimiento educativo laboral que relaciona con los documentos")
public class SeguimientoEducativoLaboralDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idSeguimientoEducativoLaboralDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_seguimiento_educativo_laboral", referencedColumnName = "idSeguimientoEducativoLaboral")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del seguimiento al que esta asociado")
    private SeguimientoEducativoLaboralOtros seguimientoEducativoLaboralOtros;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
