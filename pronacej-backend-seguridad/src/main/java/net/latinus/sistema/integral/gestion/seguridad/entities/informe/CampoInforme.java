package net.latinus.sistema.integral.gestion.seguridad.entities.informe;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "inf_campo")
@Comment("Tabla de campos de plantillas")
@EqualsAndHashCode(of = {"idCampo"}, callSuper = true)
public class CampoInforme extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador único del campo")
    private Long idCampo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plantilla_informe", nullable = false)
    @Comment("Plantilla a la que pertenece el campo")
    private PlantillaInforme plantillaInforme;

    @Column(name = "etiqueta", length = 128, nullable = false)
    @Comment("Etiqueta del campo")
    private String etiqueta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", nullable = false)
    @Comment("Referencia al catalogo de tipo de campo")
    private Catalogo tipo;
}
