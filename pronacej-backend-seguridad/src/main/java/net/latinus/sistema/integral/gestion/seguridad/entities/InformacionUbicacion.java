package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.text.SimpleDateFormat;
import org.hibernate.annotations.Comment;


@Entity
@Data
@Table(name = "seg_informacion_ubicacion")
@EqualsAndHashCode(of = {"idInformacionUbicacion"}, callSuper = true)
public class InformacionUbicacion extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la informacion")
    private Long idInformacionUbicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona_relacionada", referencedColumnName = "idPersonasRelacionadas")
    @Comment("id de la persona relacionada")
    private PersonaRelacionada idPersonasRelacionadas;

    @Comment("id del tipo de informacion ubicacion")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipoInformacionUbicacion", referencedColumnName = "idCatalogo")
    private Catalogo tipoInformacionUbicacion;

    @Comment("valor")
    private String valor;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

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
