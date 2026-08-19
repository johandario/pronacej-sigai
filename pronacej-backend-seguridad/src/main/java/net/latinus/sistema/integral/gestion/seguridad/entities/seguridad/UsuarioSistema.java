package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import net.latinus.sistema.integral.gestion.seguridad.entities.Funcionario;

@Entity
@Data
@Table(name = "seg_usuario_sistema")
@EqualsAndHashCode(of = {"idUsuarioSistema"}, callSuper = true)
@Comment("Tabla de los usuarios del sistema")
public class UsuarioSistema extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de la tabla")
    private Long idUsuarioSistema;

    @Comment("Nombre del usuario del sistema")
    private String nombres;

    @Comment("Apellidos del usuario del sistema")
    private String apellidos;

    @Comment("Username del usuario del sistema")
    private String userName;

    @Comment("Email del usuario del sistema")
    private String email;

    @Comment("Tipo del documento del usuario del sistema")
    @JoinColumn(name = "tipo_documento", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo tipoDeDocumento;

    @Comment("Numero del documento del usuario del sistema")
    private String numeroDeDocumento;

    @Comment("Telefono del usuario del sistema")
    private String telefono;

    @Comment("Telefono celular del usuario del sistema")
    private String numeroDeCelular;

    @Comment("Url del logo del usuario del sistema")
    @Column(columnDefinition = "TEXT")
    private String urlLogo = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQAFM_xyIubtJwKiuFsU3IsBZqxlRbneCKvei3_rifJE098371NG05Ptm0cfoLoAqSrXCg&usqp=CAU";

    @Comment("Id del estado del usuario del sistema")
    @JoinColumn(name = "id_estado", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    private Catalogo estado;
        
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario", referencedColumnName = "idFuncionario")
    private Funcionario funcionario;

    public UsuarioSistemaDTO convertirADTO(){
        UsuarioSistemaDTO usuarioSistemaDTO = new UsuarioSistemaDTO();
        usuarioSistemaDTO.setApellidos(this.apellidos);
        usuarioSistemaDTO.setBloqueado(this.getBloqueado());
        usuarioSistemaDTO.setLogo(this.urlLogo);
        usuarioSistemaDTO.setNombres(this.nombres);
        usuarioSistemaDTO.setApellidos(this.apellidos);
        usuarioSistemaDTO.setNumeroDeCelular(this.numeroDeCelular);
        usuarioSistemaDTO.setTelefono(this.telefono);
        usuarioSistemaDTO.setFechaCreacion(this.getFechaCreacion());
        usuarioSistemaDTO.setIpCrea(this.getIpCrea());
        usuarioSistemaDTO.setTokenIdentificador(this.getTokenIdentificador());

        return usuarioSistemaDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }
}
