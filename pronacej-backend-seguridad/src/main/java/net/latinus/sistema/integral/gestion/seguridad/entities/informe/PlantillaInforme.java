package net.latinus.sistema.integral.gestion.seguridad.entities.informe;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "inf_plantilla_informe")
@Comment("Tabla de plantillas para informes")
@EqualsAndHashCode(of = {"idPlantillaInforme"}, callSuper = true)
public class PlantillaInforme extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plantilla_informe")
    @Comment("Identificador único de la plantilla")
    private Long idPlantillaInforme;

    @Column(name = "nombre", nullable = false, length = 256)
    @Comment("Nombre o titulo del informe")
    private String nombre;

    @Column(name = "descripcion")
    @Comment("Descripcion del informe")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo", nullable = false)
    @Comment("Referencia al catalogo de la plantilla")
    private Catalogo catalogo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_centro", nullable = false)
    @Comment("Referencia al catalogo de tipo de centro")
    private Catalogo tipoCentro;

    @Column(name = "version")
    @Comment("Versión de la evaluacion")
    private Integer version = 1;
}
