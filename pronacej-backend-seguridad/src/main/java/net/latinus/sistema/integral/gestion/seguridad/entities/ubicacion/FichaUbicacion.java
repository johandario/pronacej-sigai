package net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_ficha_ubicacion")
@Comment("Tabla de ubicación por ficha")
@EqualsAndHashCode(of = {"idFichaUbicacion"}, callSuper = true)
public class FichaUbicacion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idFichaUbicacion;

    @Comment("id de la ficha de identificación")
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    private FichaIdentificacion fichaIdentificacion;

    @Comment("id de la ubicación jerárquica")
    @JoinColumn(name = "id_ubicacion_jerarquia", referencedColumnName = "idUbicacionJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    private UbicacionJerarquia ubicacionJerarquia;

    @Comment("id del centro")
    @JoinColumn(name = "id_centro")
    @ManyToOne(fetch = FetchType.LAZY)
    private Jerarquia centro;

    @Comment("fecha de ingreso")
    private Date fechaIngreso;

    @Comment("fecha de salida")
    private Date fechaSalida;

    @Comment("ubicación actual")
    private Boolean ubicacionActual = false;

    @Comment("número de cama")
    private Long numeroCama;

    @Comment("Pertenece a atención prioritaria")
    private Boolean atencionPrioritaria = false;

    @Comment("Ingresa con expediente después del traslado")
    private Boolean ingresoExpediente = false;

    @Comment("observaciones")
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

