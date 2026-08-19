package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.doc.Carpeta;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "not_notificacion")
@EqualsAndHashCode(of = {"idNotificacion"}, callSuper = true)
@Comment("Tabla de notificaciones")
public class Notificacion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    @Comment("Id de notificaciones del sistema")
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Adolescente al que pertenece la notificacion")
    private FichaIdentificacion fichaIdentificacion;

    @Comment("Remitente de la notificacion")
    @Column(name = "remitente", nullable = false, length = 256)
    private String remitente;

    @Comment("Destinatario de la notificacion")
    @Column(name = "destinatario", nullable = false,columnDefinition = "TEXT")
    private String destinatario;

    @Comment("Cuerpo de la notificacion")
    @Column(name = "cuerpo", nullable = false,columnDefinition = "TEXT")
    private String cuerpo;

    @Comment("Asunto de la notificacion")
    @Column(name = "asunto", nullable = false,columnDefinition = "TEXT")
    private String asunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", nullable = false)
    @Comment("Referencia al catalogo de tipo de notificacion")
    private Catalogo tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medio", nullable = false)
    @Comment("Medio por el cual se realiza la notificacion")
    private Catalogo medio;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa a que pertenece el documento")
    private Empresa empresa;

    @JoinColumn(name = "id_carpeta", referencedColumnName = "idCarpeta")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la carpeta donde estaran los adjuntos enviados")
    private Carpeta carpeta;

    @Comment("Observaciones de la entrega")
    @Column(name = "observaciones_entrega",columnDefinition = "TEXT")
    private String observacionesEntrega;

    @Comment("Persona(s) a la que se hace entrega")
    @Column(name = "entregado")
    private String entregado;

    @Comment("Fecha en la que se hace entrega")
    @Column(name = "fecha_entrega")
    private Date fechaEntrega;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
