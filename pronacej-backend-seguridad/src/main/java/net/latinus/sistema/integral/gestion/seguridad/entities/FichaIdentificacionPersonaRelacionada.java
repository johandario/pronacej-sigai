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
@Table(name = "seg_ficha_identificacion_persona_relacionada")
@EqualsAndHashCode(of = {"idFichaIdentificacionPersonaRelacionada"})
public class FichaIdentificacionPersonaRelacionada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idFichaIdentificacionPersonaRelacionada;

    @Comment("id de la ficha identificacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ficha_identificacion", referencedColumnName = "idFichaIdentificacion")
    private FichaIdentificacion idFichaIdentificacion;

    @Comment("id de la persona relacionadas")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personas_relacionadas", referencedColumnName = "idPersonasRelacionadas")
    private PersonaRelacionada idPersonasRelacionadas;

    @Comment("removido")
    private boolean removido;

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
