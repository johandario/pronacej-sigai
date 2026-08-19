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
@Table(name = "seg_jerarquia")
@Comment("Tabla de Jerarquias del Sistema")
@EqualsAndHashCode(of = {"idJerarquia"}, callSuper = true)
public class Jerarquia extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idJerarquia;

    @JoinColumn(name = "id_jerarquia_padre", referencedColumnName = "idJerarquia")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Identificador del padre de la jerarquía")
    private Jerarquia jerarquiaPadre;

    @Comment("Nombre de la jerarquía")
    private String nombre;
    
    @Comment("Palabra clave de la jerarquia")
    private String nemonico;
    
    @Comment("Codigo del ubigeo asociado a localidades para obtener departamento/provincia/pais")
    private String ubigeo;

    @Comment("Direccion del centro")
    private String direccion;
    
    @Comment("Coordenadas del centro")
    private String coordenadas;
    
    @Comment("Genero del centro masculino o femenino")
    @JoinColumn(name = "genero", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo genero;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Comment("Localidad del centro")
    @ManyToOne
    @JoinColumn(name = "id_localidad_centro", referencedColumnName = "idLocalidad")
    private Localidad localidadCentro;

    @Comment("Booleano para saber si es oficina central")
    @Column(columnDefinition = "boolean default false")
    private Boolean esOficinaCentral = false;

    @Comment("No mostrar en front")
    private Boolean noMostrarEnFront = false;

    private static final Logger logger = Logger.getLogger(Jerarquia.class.getName());

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
