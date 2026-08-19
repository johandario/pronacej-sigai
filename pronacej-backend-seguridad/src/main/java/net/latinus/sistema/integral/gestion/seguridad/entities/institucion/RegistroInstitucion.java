package net.latinus.sistema.integral.gestion.seguridad.entities.institucion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;
import java.text.SimpleDateFormat;
import java.util.UUID;

@Entity
@Data
@Table(name = "reg_institucion")
@Comment("Tabla que registra las instituciones")
@EqualsAndHashCode(of = {"idRegistroInstitucion"},callSuper = true)

public class RegistroInstitucion extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id del registro de la institucion")
    private Long idRegistroInstitucion;

    @Comment("Token Identificador")
    private String tokenIdentificador = UUID.randomUUID().toString();

    @Comment("nombre de la organizacion")
    private String nombreOrganizacion;

    @Comment("nombre del director o representante legal")
    private String nombreDirector;

    private String ruc;

    private String nombContactoOperacional;

    private String direccion;

    private String telefono;

    private String fax;

    private String email;

    @Comment("facebook o pagina web, url")
    private String sitioWeb;

    private String dni;

    private String misionInstitucional;

    private String objetivoInstitucional;

    @Comment("Departamento o area de servicios")
    private String departamento;

    @Comment("servicios que brinda")
    private String servicios;

    private String beneficios;

    private String horariosServicios;

    @Comment("Servicios articulados con el SOA")
    private String serviciosArticulados;

    @Comment("area geografica que abarca")
    private String areaGeografica;

    @Comment("participacion en espacios locales")
    private String participacionEspaciosLocales;

    @Comment("otras redes de las que forma parte")
    private String otroSitioWeb;

    @ManyToOne
    @JoinColumn(name = "id_catalogo_organizacion", referencedColumnName = "idCatalogo")
    @Comment("Tipo de organizacion que tiene la institucion")
    private Catalogo tipoOrganizacion;

    @Comment("Campo Cuenta con convenio de cooperación o acta de colaboración ")
    private Boolean tieneConvenio ;

    @Comment("Codigo ubigeo ")
    private String codigoUbigeoUbicacion;

    @Comment("tipo de institucion cooperantes o receptoras ")
    private String tipoInstitucion;

    private String finalidadInstitucion;

    private String estado;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_centro", nullable = true)
    @Comment("Centro")
    private Jerarquia centro;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
