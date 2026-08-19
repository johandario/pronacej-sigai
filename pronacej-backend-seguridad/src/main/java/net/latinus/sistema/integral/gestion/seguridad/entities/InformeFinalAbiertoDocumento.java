package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_informe_final_abierto_documento")
@EqualsAndHashCode(of = {"idInformeFinalAbiertoDocumento"}, callSuper = true)
@Comment("Tabla de informe final abierto que relaciona con los documentos")
public class InformeFinalAbiertoDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idInformeFinalAbiertoDocumento;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_informe_final_abierto", referencedColumnName = "idInformeFinalAbierto")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del informe al que esta asociado")
    private InformeFinalAbierto informeFinalAbierto;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
