package net.latinus.sistema.integral.gestion.seguridad.entities.informe;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "inf_informe")
@Comment("Tabla de Informes")
@EqualsAndHashCode(of = {"idInforme"}, callSuper = true)
public class Informe extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    @Comment("Identificador único del informe")
    private Long idInforme;

    @Column(name = "fecha_registro", nullable = false)
    @Comment("Fecha de registro del informe")
    private Date fechaRegistro = new Date();

    @Column(name = "impreso", nullable = false)
    @Comment("Bandera para validar si ya ha sido impreso")
    private Boolean impreso = false;

    @Column(name = "firmado")
    @Comment("Bandera para validar si ya ha sido firmado")
    private Boolean firmado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion", nullable = false)
    @Comment("Adolescente al que pertenece el informe")
    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plantilla_informe", nullable = false)
    @Comment("Referencia a la plantilla del informe")
    private PlantillaInforme plantillaInforme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_informe_padre")
    @Comment("Identificador del padre del informe")
    private Informe informePadre;
}
