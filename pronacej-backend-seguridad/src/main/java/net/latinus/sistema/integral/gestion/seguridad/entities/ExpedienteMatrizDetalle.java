package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "ia_expediente_matriz_detalle")
@Comment("Encabezado de expedientes matriz")
@EqualsAndHashCode(of = {"idExpedienteDetalle"}, callSuper = true)
public class ExpedienteMatrizDetalle extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idExpedienteDetalle;

    @JoinColumn(name = "id_catalogo_tipo_registro", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de registro")
    private Catalogo tipoRegistro;

    @JoinColumn(name = "id_catalogo_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Estado de registro")
    private Catalogo estado;

    @JoinColumn(name = "id_catalogo_situacion_juridica", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Situación juridica actual")
    private Catalogo situacionJuridica;

    @JoinColumn(name = "id_catalogo_variacion_medida", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Variacion de la medida")
    private Catalogo variacionMedida;

    @JoinColumn(name = "id_catalogo_tipo_variacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de variación en caso de existir")
    private Catalogo tipoVariacion;

    @JoinColumn(name = "id_catalogo_motivo_variacion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Motivo de la variación en caso de existir")
    private Catalogo motivoVariacion;

    @Comment("Número de resolución")
    private String numResolucion;

    @Comment("Fecha de resolución")
    private Date fechaResolucion;

    @Comment("Decisión tomada")
    private String decision;

    @Comment("Tiempo de medida socioeconómica en años")
    private Integer tiempoMedSocEduAnios;

    @Comment("Tiempo de medida socioeconómica en meses")
    private Integer tiempoMedSocEduMeses;

    @Comment("Tiempo de medida socioeconómica en días")
    private Integer tiempoMedSocEduDias;

    @Comment("Fecha en el que empieza la medida")
    private Date fechaInicioMedida;

    @Comment("Fecha final calculada de acuerdo al tiempo de medida y la fecha de inicio")
    private Date fechaFinMedida;

    @JoinColumn(name = "id_catalogo_corte_justicia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Corte superior de justicia")
    private Catalogo corteJusticia;

    @JoinColumn(name = "id_catalogo_instancia", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Corte superior de justicia")
    private Catalogo instancia;

    @JoinColumn(name = "id_catalogo_especialidad", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Corte superior de justicia")
    private Catalogo especialidad;

    @Comment("Juzgado al que pertenece")
    private String organoJurisdiccional;

    @Comment("Juez asignado")
    private String juez;

    @Comment("Secretario judicial especialista legal")
    private String secretario;

    // -- CJDR --
    @JoinColumn(name = "id_catalogo_sancion_impuesta", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Sanción impuesta (intervención preventiva o internación)")
    private Catalogo sancionImpuesta;

    @Comment("Valor del importe respectivo de reparación civil")
    @Column(precision = 38, scale = 2)
    @Digits(integer = 32, fraction = 2)
    private BigDecimal montoReparacion;

    // -- SOA --
    @JoinColumn(name = "id_catalogo_tipo_medida", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Tipo de medida socioeconómica asginada de acuerdo a tipo de infracción")
    private Catalogo tipoMedSocEduImp;

    @Comment("Lugar de infracción")
    private String lugarInfraccion;

    @Comment("Número de jornadas a cumplir")
    private Integer numJornadas;

    @JoinColumn(name = "id_catalogo_frecuencia_ingreso", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Frecuencia de ingreso, si es primera vez o si vuelve a ingresar")
    private Catalogo frecuenciaIngreso;

    @OneToMany(mappedBy = "expedienteMatrizDetalle", fetch = FetchType.LAZY)
    @Comment("Lista de delitos")
    private List<ExpedienteMatrizDelito> expedienteDelitos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_expediente")
    @Comment("Encabezado al que pertence el detalle")
    private ExpedienteMatriz expedienteMatriz;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Empresa a la que pertenence el expediente")
    private Empresa empresa;

    @OneToMany(mappedBy = "expedienteDetalleMedidaSocioeducativa", fetch = FetchType.LAZY)
    @Comment("Lista de medidas socioeducativas")
    private List<ExpedienteMatrizMedida> medidasSocioeducativas;

    @OneToMany(mappedBy = "expedienteDetalleMedidaAccesoria", fetch = FetchType.LAZY)
    @Comment("Lista de medidas socioeducativas")
    private List<ExpedienteMatrizMedida> medidasAccesorias;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
