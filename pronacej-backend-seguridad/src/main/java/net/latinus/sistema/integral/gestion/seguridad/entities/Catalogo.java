package net.latinus.sistema.integral.gestion.seguridad.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "par_catalogo")
@EqualsAndHashCode(of = {"idCatalogo"}, callSuper = true)
@Comment("Tabla de catalogo")
public class Catalogo extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idCatalogo;

    @Comment("nombre del catalogo")
    private String nombre;

    @Comment("nemonico del catalago")
    private String nemonico;

    @Comment("Descripción del catalogo")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Comment("id del catalogo padre")
    @JoinColumn(name = "id_catalogo_padre", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo catalogoPadre;

    @Comment("codigo externo del catalogo")
    private String codigoExterno;

    @Comment("id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    public CatalogoDTO convertirADTO() {
        CatalogoDTO objetoDTO = new CatalogoDTO();
        objetoDTO.setTokenIdentificador(super.getTokenIdentificador());
        objetoDTO.setNemonico(this.getNemonico());
        objetoDTO.setDescripcion(this.getDescripcion());
        objetoDTO.setFechaCreacion(this.getFechaCreacion());
        objetoDTO.setCodigoExterno(this.getCodigoExterno());
        objetoDTO.setNombre(this.getNombre());
        objetoDTO.setTokenIdentificadorPadre(catalogoPadre != null ? catalogoPadre.getTokenIdentificador() : null);
        Empresa empresa = this.getEmpresa();

        objetoDTO.setTokenIdentificadorEmpresa(empresa != null ? empresa.getTokenIdentificador() : null);
        return objetoDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}