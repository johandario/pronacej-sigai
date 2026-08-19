package net.latinus.sistema.integral.gestion.seguridad.entities.informe;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "inf_informe_documento")
@EqualsAndHashCode(of = {"idInformeDocumento"}, callSuper = true)
@Comment("Tabla de informes que relaciona con los documentos")
public class InformeDocumento extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idInformeDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_informe", referencedColumnName = "id_informe")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del informe al que esta asociado")
    private Informe informe;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
