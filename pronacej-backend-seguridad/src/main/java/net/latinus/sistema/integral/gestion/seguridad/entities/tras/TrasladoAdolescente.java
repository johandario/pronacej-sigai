package net.latinus.sistema.integral.gestion.seguridad.entities.tras;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "tras_traslado_adolescente")
@Comment("Tabla intermedia para relacionar adolescentes con traslados")
@EqualsAndHashCode(of = {"idTrasladoAdolescente"}, callSuper = true)
public class TrasladoAdolescente extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idTrasladoAdolescente;

    @ManyToOne
    @JoinColumn(name = "id_traslado")
    @Comment("Traslado al que pertenece")
    private Traslado traslado;

    @ManyToOne
    @JoinColumn(name = "id_ficha_identificacion")
    @Comment("Adolescente relacionado al traslado")
    private FichaIdentificacion fichaIdentificacion;

    @Comment("Campo cuando ya se ha completado la solicitud de traslado")
    private Boolean completado;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_estado_evento")
    @Comment("Estado que tiene el evento")
    private Catalogo estadoEvento;


    @Comment("Campo que indica si el proceso finalizo con registro de salida")
    private Boolean isComplete ;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
