package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_seguimiento_social_documento")
@EqualsAndHashCode(of = {"idSeguimientoSocialDocumento"}, callSuper = true)
@Comment("Tabla de seguimiento educativo laboral que relaciona con los documentos")
public class SeguimientoSocialDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idSeguimientoSocialDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_seguimiento_social", referencedColumnName = "idSeguimientoSocial")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del seguimiento al que esta asociado")
    private SeguimientoSocial seguimientoSocial;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
