package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.PasswordUserSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Data
@Table(name = "seg_password_usuario_sistema")
@EqualsAndHashCode(of = {"idPassword"}, callSuper = true)
@Comment("Tabla de las contrasenia de los usuarios del sistema")
public class PasswordUserSistema extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idPassword;

    @Column(columnDefinition = "TEXT")
    @Comment("Contrasenia encriptada con bcrypt")
    private String password;

    @Column(columnDefinition = "TEXT")
    @Comment("Contrasenia encriptada con aes")
    private String passwordEncrypt;

    @Comment("Declara si la contrasenia nunca expira")
    private Boolean nuncaExpira = false;

    @Comment("Fecha que se habilito la contrasenia")
    private Date fechaHabilitada = new Date();

    @Comment("Id del usuario del sistema a quien pertenece la contrasenia")
    @JoinColumn(name = "id_usuario_sistema", referencedColumnName = "idUsuarioSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    private UsuarioSistema usuarioSistema;

    public PasswordUserSistemaDTO convertirADTO() {
        PasswordUserSistemaDTO passwordUserSistemaDTO = new PasswordUserSistemaDTO();
        passwordUserSistemaDTO.setPassword(this.password);
        passwordUserSistemaDTO.setPasswordEncrypt(this.passwordEncrypt);
        passwordUserSistemaDTO.setFechaHabilitada(this.fechaHabilitada);
        passwordUserSistemaDTO.setNuncaExpira(this.nuncaExpira);
        passwordUserSistemaDTO.setFechaCreacion(this.getFechaCreacion());
        passwordUserSistemaDTO.setIpCrea(this.getIpCrea());
        passwordUserSistemaDTO.setTokenIdentificador(this.getTokenIdentificador());

        return passwordUserSistemaDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }

}
