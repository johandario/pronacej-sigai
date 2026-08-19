package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

@Entity
@Data
@Table(name = "seg_cargos_jerarquia")
@Comment("Tabla de Cargos para las jerarquias del Sistema")
@EqualsAndHashCode(of = {"idCargosJerarquia"}, callSuper = true)
public class CargosJerarquia extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idCargosJerarquia;

    @JoinColumn(name = "id_jerarquia", referencedColumnName = "idJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Identificador de la jerarquía del departamento del cargo")
    private Jerarquia jerarquia;

    @Column(columnDefinition = "boolean default false")
    @Comment("declara si es jefe")
    private Boolean esJefe = false;

    @Comment("Nombre del cargo")
    private String nombre;
    
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("id de la empresa")
    private Empresa empresa;

    private static final Logger logger = Logger.getLogger(CargosJerarquia.class.getName());

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setDateFormat(new SimpleDateFormat(
                    EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER));
            ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

            return  ow.writeValueAsString(this);
        }catch (Exception ex){
            logger.info(ex.getMessage());
            return "";
        }
    }
}
