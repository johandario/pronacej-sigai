package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_direccion_persona_referencia")
@EqualsAndHashCode(of = {"idDireccionPersonaReferencia"})
public class DireccionPersonaReferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idDireccionPersonaReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoDireccion", referencedColumnName = "idCatalogo")
    @Comment("id del tipo de direccion")
    private Catalogo tipoDireccion;

    @Comment("direccion")
    private String direccion;

    @Comment("telefono")
    private String telefono;

    @Comment("celular")
    private String celular;

    @Comment("correo electronico")
    private String email;

    @Comment("removido")
    private boolean removido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona_relacionada", referencedColumnName = "idPersonasRelacionadas")
    @Comment("id de la persona relacionada")
    private PersonaRelacionada idPersonasRelacionadas;

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(
                    EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

            return ow.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }
}
