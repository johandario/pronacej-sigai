package net.latinus.sistema.integral.gestion.seguridad.entities.ubicacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.logging.Logger;

@Entity
@Data
@Table(name = "seg_ubicacion_jerarquia")
@Comment("Tabla de ubicaciones jerárquicas")
@EqualsAndHashCode(of = {"idUbicacionJerarquia"}, callSuper = true)
public class UbicacionJerarquia extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idUbicacionJerarquia;

    @JoinColumn(name = "id_ubicacion_jerarquia_padre")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Identificador del padre de la ubicación")
    private UbicacionJerarquia ubicacionJerarquiaPadre;

    @JoinColumn(name = "id_jerarquia_tipo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Identificador del padre de la ubicación")
    private Jerarquia jerarquiaTipo;

    @JoinColumn(name = "id_jerarquia_centro")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Identificador del padre de la ubicación")
    private Jerarquia jerarquiaCentro;

    @Comment("Nombre de ubicación")
    private String nombre;

    @Comment("Nombre corto de ubicación")
    private String nombreCorto;

    @Column(columnDefinition = "TEXT")
    @Comment("Descripción de ubicación")
    private String descripcion;

    @Comment("Tipo de sexo")
    @JoinColumn(name = "id_catalogo_tipo_sexo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoSexo;

    @Comment("Atención prioritaria")
    @JoinColumn(name = "id_catalogo_atencion_prioritaria")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo atencionPrioritaria;

    @Comment("Tipo de ubicación")
    @JoinColumn(name = "id_catalogo_tipo_ubicacion")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoUbicacion;

    @Comment("Rango inicial de un valor numérico")
    private Long rangoInicio;

    @Comment("Rango final de un valor numérico")
    private Long rangoFin;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;
    
    private static final Logger logger = Logger.getLogger(UbicacionJerarquia.class.getName());

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
