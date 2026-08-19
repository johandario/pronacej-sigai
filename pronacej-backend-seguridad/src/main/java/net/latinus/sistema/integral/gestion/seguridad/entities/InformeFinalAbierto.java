package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "seg_informe_final_abierto")
@Comment("Encabezado de informe final para régimen abierto")
@EqualsAndHashCode(of = {"idInformeFinalAbierto"}, callSuper = true)
public class InformeFinalAbierto extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idInformeFinalAbierto;

    @Comment("Fortalecimiento de derechos ciudadanos")
    @Column(columnDefinition = "TEXT")
    private String fortalecimientoDerechos;

    @Comment("Área educativa/laboral/productivo")
    @Column(columnDefinition = "TEXT")
    private String area;

    @Comment("Fortalecimiento familiar")
    @Column(columnDefinition = "TEXT")
    private String fortalecimientoFamiliar;

    @Comment("Intervención psicosocial")
    @Column(columnDefinition = "TEXT")
    private String intervencion;

    @Comment("Enfoque restaurativo")
    @Column(columnDefinition = "TEXT")
    private String enfoque;

    @Comment("Cultural, artístico deportivo y acciones cívicas")
    @Column(columnDefinition = "TEXT")
    private String cultural;

    @Comment("Responsabilidad de la conducta infractora")
    @Column(columnDefinition = "TEXT")
    private String responsabilidad;

    @Comment("Toma conciencia de la medida socioeducativa")
    @Column(columnDefinition = "TEXT")
    private String conciencia;

    @OneToMany(mappedBy = "informeFinalAbierto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Comment("Medidas accesorias de Informe Final")
    private List<InformeFinalAbiertoMedidas> medidasList;

    @Comment("Valoración del riesgo de reincidencia")
    @Column(columnDefinition = "TEXT")
    private String valoracionRiesgo;

    @Comment("Conclusiones y/o recomendaciones")
    @Column(columnDefinition = "TEXT")
    private String conclusionesRecomendaciones;

    @Comment("Bandera para saber si es borrador o no")
    @Column(name = "completado")
    private Boolean completado;

    @Comment("Fecha en la que se completa el informe")
    @Column(name = "fecha_finalizacion")
    private Date fechaFinalizacion;

    @ManyToOne
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Ficha de identificación padre")
    private FichaIdentificacion fichaIdentificacion;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Empresa a la que pertenence el expediente")
    private Empresa empresa;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
