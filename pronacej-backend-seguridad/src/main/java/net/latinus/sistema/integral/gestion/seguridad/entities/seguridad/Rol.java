package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_rol")
@EqualsAndHashCode(of = {"idRol"}, callSuper = true)
@Comment("Tabla de los roles del sistema")
public class Rol extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idRol;

    @Comment("Nombre del rol")
    private String nombre;

    @Comment("Codigo del rol")
    private String codigo;

    @Comment("Descripcion del rol")
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Comment("Declare si es un super rol")
    private Boolean esSuperRol = false;

    @Comment("Id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Comment("Declara si es un rol a ser asignado a los usuario por defecto, debe de existir solo uno")
    private Boolean esRolPorDefecto = false;

    @Comment("Dias que tendra el usuario para cambiar su contrasenia")
    private Integer diasExpiracionPassword = 30;

    public RolDTO convertirADTO() {
        RolDTO rolDTO = new RolDTO();
        rolDTO.setEsSuperRol(this.esSuperRol);
        rolDTO.setCodigo(this.codigo);
        rolDTO.setNombre(this.nombre);
        rolDTO.setEsRolPorDefecto(this.esRolPorDefecto);
        rolDTO.setDescripcion(this.descripcion);
        rolDTO.setDiasExpiracionPassword(this.diasExpiracionPassword);
        rolDTO.setFechaCreacion(this.getFechaCreacion());
        rolDTO.setTokenIdentificador(this.getTokenIdentificador());
        rolDTO.setTokenIdentificadorEmpresa(this.empresa != null ? empresa.getTokenIdentificador() : null);

        return rolDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }
}
