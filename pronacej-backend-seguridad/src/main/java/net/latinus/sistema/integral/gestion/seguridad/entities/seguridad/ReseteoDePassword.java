package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.ReseteoDePasswordDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;

@Entity
@Data
@Table(name = "seg_reseteo_de_password")
@EqualsAndHashCode(of = {"idReseteoDePassword"}, callSuper = true)
@Comment("Tabla de reseteos de contrasenia de los usuarios")
public class ReseteoDePassword extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idReseteoDePassword;

    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del estado del reseteo")
    private Catalogo estado;

    @JoinColumn(name = "id_usuario_sistema_empresa_rol", referencedColumnName = "idUsuarioSistemaEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del usuario sistema empresa rol")
    private UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @JoinColumn(name = "id_contrasenia_user_sistema", referencedColumnName = "idPassword")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la contrasenia asociada a este reseteo, solo existe si el proceso termino con exito")
    private PasswordUserSistema passwordUserSistema;

    public ReseteoDePasswordDTO convertirADTO() {
        ReseteoDePasswordDTO reseteoDePasswordDTO = new ReseteoDePasswordDTO();
        reseteoDePasswordDTO.setEmpresaDTO(this.empresa != null ? this.empresa.convertirADTO() : null);
        reseteoDePasswordDTO.setEstadoDTO(this.estado != null ? this.estado.convertirADTO() : null);
        reseteoDePasswordDTO.setPasswordUserSistemaDTO(this.passwordUserSistema != null ? this.passwordUserSistema.convertirADTO() : null);
        reseteoDePasswordDTO.setUsuarioSistemaEmpresaRolDTO(this.usuarioSistemaEmpresaRol != null ? this.usuarioSistemaEmpresaRol.convertirADTO() : null);

        reseteoDePasswordDTO.setFechaCreacion(this.getFechaCreacion());
        reseteoDePasswordDTO.setIpCrea(this.getIpCrea());
        reseteoDePasswordDTO.setTokenIdentificador(this.getTokenIdentificador());
        reseteoDePasswordDTO.setTokenIdentificadorEmpresa(this.empresa != null ? this.empresa.getTokenIdentificador() : null);

        return reseteoDePasswordDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }
}
