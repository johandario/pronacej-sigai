package net.latinus.sistema.integral.gestion.seguridad.entities.ia;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.ExpedienteMatriz;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "ia_acta_externamiento")
@EqualsAndHashCode(of = {"idActaExternamiento"}, callSuper = true)
@Comment("Tabla de actas de externamiento")
public class ActaExternamiento extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acta_externamiento")
    @Comment("Id del acta")
    private Long idActaExternamiento;

    @Column(name = "fecha_registro", nullable = false)
    @Comment("Fecha en la que se registra el acta")
    private Date fechaRegistro;

    @Comment("A qué ingreso corresponde el externamiento")
    @Column(name = "ingreso", nullable = false)
    private String ingreso;

    @Comment("Por disposición de qué juzgado o institución se realizará la salida")
    @Column(name = "institucion", nullable = false)
    private String institucion;

    @Comment("Por quién está dada la autorización")
    @Column(name = "autorizacion", nullable = false)
    private String autorizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipoDocumento", nullable = false)
    @Comment("Tipo de Documento")
    private Catalogo tipoDocumento;

    @Comment("Número del documento")
    @Column(name = "numero_documento", nullable = false)
    private String numeroDocumento;

    @Comment("Resolución por la que se da el externamiento")
    @Column(name = "resolucion", nullable = false, columnDefinition = "TEXT")
    private String resolucion;

    @Comment("Domicilio donde residirá el adolescente")
    @Column(name = "domicilio", nullable = false)
    private String domicilio;

    @Comment("Si es que cuenta con mandato de detención vigente notificado")
    @Column(name = "mandato_detencion", nullable = false)
    private Boolean mandatoDetencion;

    @Comment("Si es que se retira Solo ")
    @Column(name = "retiro_solo", nullable = false)
    private Boolean retiroSolo;

    @Comment("Familiares que lo retiran")
    @Column(name = "familiares")
    private String familiares;

    @Comment("Parentescos de los familiares")
    @Column(name = "parentescos")
    private String parentescos;

    @Comment("Identificaciones de los familiares")
    @Column(name = "identificaciones")
    private String identificaciones;

    @Comment("Direcciones de los familiares")
    @Column(name = "direcciones")
    private String direcciones;

    @Comment("Teléfonos de los familiares")
    @Column(name = "telefonos")
    private String telefonos;

    @Comment("Observaciones/Comentarios")
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Comment("Si ya se ha impreso")
    @Column(name = "impreso")
    private Boolean impreso = false;

    @Comment("Si ya se ha firmado")
    @Column(name = "firmado")
    private Boolean firmado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_expediente_matriz", nullable = false)
    @Comment("Referencia al expediente matriz")
    private ExpedienteMatriz expedienteMatriz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion", nullable = false)
    @Comment("Referencia a la ficha de identificacion")
    private FichaIdentificacion fichaIdentificacion;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado_evento")
    @Comment("Estado que tiene el evento")
    private Catalogo estadoEvento;

    @Comment("Campo que indica si el proceso finalizo con registro de salida")
    private Boolean isComplete = false;;
}
