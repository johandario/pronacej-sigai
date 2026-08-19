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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "ia_expediente_matriz")
@Comment("Encabezado de expedientes matriz")
@EqualsAndHashCode(of = {"idExpediente"},callSuper = true)
public class ExpedienteMatriz extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idExpediente;

    @Comment("Número de expediente autogenerado de acuerdo al formato YYYY-XXXXXX, donde YYYY corresponde al año actual y XXXXXX es un número incremental, se reinicia después de cada nuevo año")
    private String numExpediente;

    @JoinColumn(name = "id_catalogo_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Estado actual del expediente")
    private Catalogo estado;

    @Comment("Número de oficio de expediente")
    private String numOficio;

    @Comment("Fecha de oficio")
    private Date fechaOficio;

    @Comment("Observación general de expediente")
    private String observacion;

    @Comment("Tipo de centro")
    private String tipoCentro;

    @Comment("Motivo de ingreso")
    private String motivoIngreso;

    @OneToMany(mappedBy = "expedienteMatriz", fetch = FetchType.LAZY)
    @Comment("Lista de registros legales")
    private List<ExpedienteMatrizDetalle> expedienteDetalle;

    @JoinColumn(name = "id_ficha_ingreso", referencedColumnName = "idFichaIngreso")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha de ingreso asociada al expediente")
    private FichaIngreso fichaIngreso;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Ficha de identificación asociada al expediente")
    private FichaIdentificacion fichaIdentificacion;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Empresa a la que pertenence el expediente")
    private Empresa empresa;

    @Comment("Numero de expediente judicial")
    @Column(columnDefinition = "TEXT")
    private String numExpedienteJudicial;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
