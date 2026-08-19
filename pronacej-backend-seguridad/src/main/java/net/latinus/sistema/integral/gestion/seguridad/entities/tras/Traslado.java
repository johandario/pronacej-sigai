package net.latinus.sistema.integral.gestion.seguridad.entities.tras;

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
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.List;

@Entity
@Data
@Table(name = "tras_traslado")
@Comment("Tabla que gestiona los procesos de traslados")
@EqualsAndHashCode(of = {"idTraslado"}, callSuper = true)
public class Traslado extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idTraslado;

    @Comment("Número de identificación de traslado")
    private String numTraslado;

    @ManyToOne
    @JoinColumn(name = "id_centro_origen")
    @Comment("Centro origen de traslado")
    private Jerarquia centroOrigen;

    @ManyToOne
    @JoinColumn(name = "id_centro_destino")
    @Comment("Centro destino de traslado")
    private Jerarquia centroDestino;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_motivo_traslado")
    @Comment("Motivo por el que se realiza el traslado")
    private Catalogo motivoTraslado;

    @Comment("Antecedentes de informe de traslado")
    @Column(columnDefinition = "TEXT")
    private String antecedentes;

    @Comment("Analisis de informe de traslado")
    @Column(columnDefinition = "TEXT")
    private String analisis;

    @Comment("Conclusiones de informe de traslado")
    @Column(columnDefinition = "TEXT")
    private String conclusiones;

    @Comment("Recomendaciones de informe de traslado")
    @Column(columnDefinition = "TEXT")
    private String recomendaciones;

    @Comment("Descripcion de la solicitu de informe director")
    @Column(columnDefinition = "TEXT")
    private String descripcionSolicitud;

    @Comment("Comentario de rechazo en caso de que no se apruebe la solicitud")
    @Column(columnDefinition = "TEXT")
    private String comentarioRechazo;

    @Comment("Campo cuando ya se ha completado la solicitud de traslado")
    private Boolean completado;

    @ManyToOne
    @JoinColumn(name = "id_instancia_proceso")
    @Comment("Instancia de proceso referente a flujo configurado")
    private InstanciaProceso instanciaProceso;

    @OneToMany(mappedBy = "traslado", cascade = CascadeType.ALL)
    @Comment("Lista de tareas")
    private List<TrasladoAdolescente> trasladoAdolescentes;

    @ManyToOne
    @JoinColumn(name = "id_estado_traslado")
    @Comment("Estado que tiene el traslado")
    private Catalogo estadoTraslado;

    @Comment("Nombre del usuario que crea el traslado en el centro origen")
    private String usuarioCreaTraslado;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
