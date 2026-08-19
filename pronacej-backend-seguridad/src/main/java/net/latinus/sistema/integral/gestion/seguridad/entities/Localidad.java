package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LocalidadDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "par_localidades")
@EqualsAndHashCode(of = {"idLocalidad"}, callSuper = true)
public class Localidad extends EntidadBase{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idLocalidad;

    @Comment("nombre")
    private String nombre;

    @Comment("nemonico")
    private String nemonico;

    @Comment("dial code")
    private String dialCode;

    @Comment("codigo iso")
    private String codigoIso;

    @Comment("genticilio")
    private String gentilicio;

    @Comment("localida padre")
    @JoinColumn(name = "id_localidad_padre", referencedColumnName = "idLocalidad")
    @ManyToOne(fetch = FetchType.LAZY)
    private Localidad localidadPadre;

    @Comment("tipo de localidad")
    @JoinColumn(name = "id_tipo", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoLocalidad;

    @Comment("codigo ubigeo")
    private String codigoUbigeo;

    @Comment("id estado")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;

    @Comment("id de la tabla")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    public LocalidadDTO convertirADTO() {
        LocalidadDTO objetoDTO = new LocalidadDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        objetoDTO.setNemonico(this.getNemonico());
        objetoDTO.setUbigeo(this.getCodigoUbigeo());
        objetoDTO.setFechaCreacion(this.getFechaCreacion());
        objetoDTO.setTipoLocalidad(this.getTipoLocalidad().getNemonico());
        objetoDTO.setNombre(this.getNombre());
        objetoDTO.setTokenIdentificadorPadre(localidadPadre != null ? localidadPadre.getTokenIdentificador() : null);
        Empresa empresa = this.getEmpresa();

        objetoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
