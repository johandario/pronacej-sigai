package net.latinus.sistema.integral.gestion.seguridad.entities.institucion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.Traslado;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "seg_institucion")
@Comment("Tabla para realizar gestion de la institucion receptora")
@EqualsAndHashCode(of = {"idSeguimientoInstitucion"}, callSuper = true)

public class SeguimientoInstitucion extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Identificador principal de tabla")
    private Long idSeguimientoInstitucion;

    @Comment("Token Identificador")
    private String tokenIdentificador = UUID.randomUUID().toString();


    @ManyToOne
    @JoinColumn(name = "id_institucion")
    private RegistroInstitucion registroInstitucion;

    @Comment("Fecha de registro")
    private Date fechaRegistro;

    @Comment("Numero del documento")
    private String numeroDoc;

    @Comment("Estado del seguimiento")
    private String estado;

    @Comment("Fecha ")
    private Date fecha;

    @Comment("Personas entrevistadas en el seguimiento")
    private String personaEntrevistada;

    private String fortalezas;

    private String debilidades;

    @Comment("Registro de si cumple con el objetivo la institucion")
    private Boolean cumpleObjetivo ;

    @Comment("Persona responsable del seguimiento")
    private String personaResponsable;


    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
