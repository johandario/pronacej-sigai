package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "seg_auditoria_servicio_rest")
@EqualsAndHashCode(of = {"idAuditoriaServicioRest"}, callSuper = true)
@Comment("Tabla de auditorias de servicios rest")
public class AuditoriaServicioRest extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Tabla de auditorias de servicios rest")
    private Long idAuditoriaServicioRest;

    @Column(columnDefinition = "TEXT")
    @Comment("Url del servicio rest consumido")
    private String url;

    @Comment("Tipo de metodo rest realizado")
    private String tipoDeMetodo;

    @Column(columnDefinition = "TEXT")
    @Comment("Json request enviado")
    private String jsonRequest;

    @Column(columnDefinition = "TEXT")
    @Comment("Json response enviado")
    private String jsonResponse;

    @Column(columnDefinition = "TEXT")
    @Comment("Headers en json de la peticion realizada")
    private String headersJson;

    @Comment("Fecha del request")
    private Date fechaRequest;

    @Comment("Fecha del response")
    private Date fechaResponse;

    @Comment("Host del servicio consumido")
    private String host;

    @Comment("Longitud del contenido")
    private Integer contentLength;

    @Column(columnDefinition = "TEXT")
    @Comment("Header de authorization")
    private String headerAuthorization;

    @Column(columnDefinition = "TEXT")
    @Comment("UserAgent del consumo")
    private String userAgent;

    @Comment("Tipo de contenido realizado")
    private String contentType;

    @Comment("Tipo de contenido aceptado")
    private String accept;

    @Comment("Tipo de plataforma")
    private String platform;

    @Comment("Origen del request")
    private String origin;

    @Comment("Referer del request")
    private String referer;

    @Comment("Lenguaje aceptado del request")
    private String acceptLanguage;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
