package net.latinus.sistema.integral.gestion.seguridad.entities.salida;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "sal_sesion_reforzamiento")
@Comment("Tabla de sesiones de reforzamiento")
@EqualsAndHashCode(of = {"idSesionReforzamiento"}, callSuper = true)
public class SesionReforzamiento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion_reforzamiento")
    @Comment("Identificador único de la sesion de reforzamiento")
    private Long idSesionReforzamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reforzamiento", nullable = false)
    @Comment("Reforzamientp al que pertenece la sesion")
    private Reforzamiento reforzamiento;

    @Column(name = "fecha_sesion", nullable = false)
    @Comment("Fecha de la sesion")
    private Date fechaSesion = new Date();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_sesion", nullable = false)
    @Comment("Tipo de la sesion")
    private Catalogo tipoSesion;

    @Column(name = "nombre_responsable", nullable = false)
    @Comment("Nombre del responsable")
    private String nombreResponsable;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    @Comment("Observaciones")
    private String observaciones;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_documento", referencedColumnName = "idDocumento")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
