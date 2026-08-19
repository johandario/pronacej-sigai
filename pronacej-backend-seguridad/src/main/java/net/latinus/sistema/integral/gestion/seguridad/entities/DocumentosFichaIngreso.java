package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "ia_documento_ficha_ingreso")
@Comment("Documentos asociados a la ficha ingreso")
@EqualsAndHashCode(of = {"idDocumentoFichaIngreso"}, callSuper = true)
public class DocumentosFichaIngreso extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del criterio de la evaluacion medica de progreso")
    private Long idDocumentoFichaIngreso;

    @JoinColumn(name = "id_tipo_documento", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Catálogo del tipo de documentos asociado")
    private Catalogo tipoDocumento;

    @JoinColumn(name = "id_ficha_ingreso", referencedColumnName = "idFichaIngreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de ingreso")
    private FichaIngreso fichaIngreso;
}
