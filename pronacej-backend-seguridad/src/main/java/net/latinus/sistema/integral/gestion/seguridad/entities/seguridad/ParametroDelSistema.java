package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ParametroDelSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "par_parametro_de_sistema")
@EqualsAndHashCode(of = {"idParametroDelSistema"}, callSuper = true)
@Comment("Tabla de parametros del sistema")
public class ParametroDelSistema extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idParametroDelSistema;

    @Comment("Nombre del parametro del sistema")
    private String nombre;

    @Comment("Nemonico del parametro del sistema")
    private String nemonico;

    @Comment("Descripción del parametro del sistema")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Comment("Valor del parametro del sistema")
    @Column(columnDefinition = "TEXT")
    private String valor;

    @Comment("Valor externo del parametro del sistema")
    private String valorExterno;

    @JoinColumn(name = "id_parametro_de_sistema_padre", referencedColumnName = "idParametroDelSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del parametro del sistema padre")
    private ParametroDelSistema parametroDelSistemaPadre;

    @Comment("Codigo externo del parametro del sistema")
    private String codigoExterno;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    public ParametroDelSistemaDTO convertirADTO(){
        ParametroDelSistemaDTO parametroDelSistemaDTO = new ParametroDelSistemaDTO();
        parametroDelSistemaDTO.setNombre(this.nombre);
        parametroDelSistemaDTO.setDescripcion(this.descripcion);
        parametroDelSistemaDTO.setNemonico(this.nemonico);
        parametroDelSistemaDTO.setCodigoExterno(this.codigoExterno);
        parametroDelSistemaDTO.setValorExterno(this.valorExterno);
        parametroDelSistemaDTO.setValor(this.valor);
        parametroDelSistemaDTO.setTokenIdentificador(this.getTokenIdentificador());

        parametroDelSistemaDTO.setTokenIdentificadorPadre(
                this.parametroDelSistemaPadre!=null? this.parametroDelSistemaPadre.getTokenIdentificador(): null
        );

        return parametroDelSistemaDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }
}