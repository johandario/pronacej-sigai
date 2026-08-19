package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Documento;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "not_notificacion_documento")
@EqualsAndHashCode(of = {"idNotificacionDocumento"}, callSuper = true)
@Comment("Tabla de notificaciones que relaciona con los documentos")
public class NotificacionDocumento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id (primary key) de la tabla")
    private Long idNotificacionDocumento;

    @JoinColumn(name = "id_carpeta", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta")
    private Carpeta carpeta;

    @JoinColumn(name = "id_notificacion", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la notificacion a la que esta asociado")
    private Notificacion notificacion;

    @JoinColumn(name = "id_documento", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del documento")
    private Documento documento;
}
