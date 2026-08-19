package net.latinus.sistema.integral.gestion.seguridad.entities.fuga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.flujo.InstanciaProceso;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "gest_evento_fuga")
@Comment("Tabla que gestiona los procesos de fuga")
@EqualsAndHashCode(of = {"idFuga"},callSuper = true)
public class EventoFuga extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la gestion de fuga")
    private Long idFuga;

    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Comment("Ficha identificacion del adolescente que se esta fugando")
    private FichaIdentificacion tokenFichaIdentificacion;


    @ManyToOne
    @JoinColumn(name = "id_catalogo_parentesco", referencedColumnName = "idCatalogo")
    @Comment("Parentesco del responsable del adolescente infractor")
    private Catalogo parentesco;

    @Comment("Fecha de registro de la fuga")
    private Date fechaRegistro = new Date();

    @Comment("Fecha de la fuga")
    private Date fechaFuga;

    @Comment("Fecha del informe del director")
    private Date fechaInformeDirector;

    @Comment("Fecha del informe del apoderado")
    private Date fechaInformeApoderado;

    @Comment("Descripcion del evento fuga")
    @Column(columnDefinition = "TEXT")
    private String descripcionHechos;

    @Comment("Acciones realizadas por en adolescente infractor")
    @Column(columnDefinition = "TEXT")
    private String accionesRealizadas;

    @Comment("Persona analista que hizo informe")
    private String presenciaDe;

    @ManyToOne
    @JoinColumn(name = "id_instancia_proceso")
    @Comment("Instancia de proceso referente a flujo configurado")
    private InstanciaProceso instanciaProceso;

    @Comment("Token Identificador")
    private String tokenIdentificador = UUID.randomUUID().toString();

    @Comment("A quien se dirigen el segundo informe")
    private String dirigidoA;

    @Comment("Asunto del segundo informe")
    private String asunto;

    @Comment("De parte de quien va el informe del segundo proceso")
    private String de;

    @Comment("Apoderado del adolescente infractor")
    private String apoderado;

    @Comment("DNI adolescente infractor")
    private String dni;

    @Comment("Número de identificación de traslado")
    private String numFuga;

    @Comment("Campo que indica si el proceso finalizo con registro de salida")
    private Boolean isComplete ;

    @ManyToOne
    @JoinColumn(name = "id_centro")
    @Comment("Centro de salida temporal")
    private Jerarquia centro;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado_evento")
    @Comment("Estado que tiene el evento")
    private Catalogo estadoEvento;

    @Comment("Campo que indica si el proceso de fuga completo todos sus pasos")
    private Boolean ultimoPaso = false;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
