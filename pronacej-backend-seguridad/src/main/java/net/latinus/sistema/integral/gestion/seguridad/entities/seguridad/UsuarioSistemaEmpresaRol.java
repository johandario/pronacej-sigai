package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaEmpresaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_usuario_sistema_empresa_rol")
@EqualsAndHashCode(of = {"idUsuarioSistemaEmpresa"}, callSuper = true)
@Comment("Tabla de usuario sistema empresa rol")
public class UsuarioSistemaEmpresaRol extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idUsuarioSistemaEmpresa;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @JoinColumn(name = "id_usuario_sistema", referencedColumnName = "idUsuarioSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de usuario sistema")
    private UsuarioSistema usuarioSistema;

    @JoinColumn(name = "id_rol", referencedColumnName = "idRol")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del rol")
    private Rol rol;

    @Comment("Declara si se usa la autenticacion de 2 pasos (envio de otp)")
    private Boolean autenticacionEn2Pasos = false;

    @Comment("Declara si debe de cambiar la contrasenia cada cierto tiempo")
    private Boolean cambioContraseniaCadaNDias = false;

    public UsuarioSistemaEmpresaRolDTO convertirADTO() {
        UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO = new UsuarioSistemaEmpresaRolDTO();

        usuarioSistemaEmpresaRolDTO.setTokenIdentificadorEmpresa(this.empresa != null ? this.empresa.getTokenIdentificador() : null);
        usuarioSistemaEmpresaRolDTO.setRolDTO(this.rol != null ? this.rol.convertirADTO() : null);
        usuarioSistemaEmpresaRolDTO.setUsuarioSistemaDTO(this.usuarioSistema != null ? this.usuarioSistema.convertirADTO() : null);
        usuarioSistemaEmpresaRolDTO.setTokenIdentificador(this.getTokenIdentificador());
        usuarioSistemaEmpresaRolDTO.setFechaCreacion(this.getFechaCreacion());
        usuarioSistemaEmpresaRolDTO.setIpCrea(this.getIpCrea());

        return usuarioSistemaEmpresaRolDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }

}
