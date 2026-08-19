package net.latinus.sistema.integral.gestion.seguridad.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_actividad_ocupacional")
@Comment("Tabla actividad ocupacional, donde se definen las actividades ocupacionales")
@EqualsAndHashCode(of = {"idActividadOcupacional"}, callSuper = true)
public class ActividadOcupacional extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idActividadOcupacional;

    @Comment("Fecha de Inicio")
    private Date fechaInicio;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_tipo_documento_aprobacion")
    @Comment("Tipo Documento Aprobación")
    private Catalogo tipoDocumentoAprobacion;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_tipo_actividad_ocupacional")
    @Comment("Tipo Actividad Ocupacional")
    private Catalogo tipoActividadOcupacional;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado_actividad_ocupacional")
    @Comment("Estado Actividad Ocupacional")
    private Catalogo estadoActividadOcupacional;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_tipo_programa_actividad_ocupacional")
    @Comment("Tipo Programa Actividad Ocupacional")
    private Catalogo tipoPrograma;

    @Comment("objetivo actividad")
    @Column(columnDefinition = "TEXT")
    private String objetivoActividad;

    @Comment("numero documento")
    private String numeroDocumento;

    @ManyToOne
    @JoinColumn(name = "id_programa_jerarquiera")
    @Comment("Programa Actividad Ocupacional")
    private Jerarquia programa;

    @ManyToOne
    @JoinColumn(name = "id_ambiente_jerarquiera")
    @Comment("Ambiente Actividad Ocupacional")
    private Jerarquia ambiente;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;

    @Comment("documento aprobacion")
    private String documentoAprobacion;
}
