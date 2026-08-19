package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.EmpresaDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

@Entity
@Data
@Table(name = "seg_empresa")
@EqualsAndHashCode(of = {"idEmpresa"}, callSuper = true)
@Comment("Tabla empresa")
public class Empresa extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id de la tabla")
    private Long idEmpresa;

    @Comment("Nombre de la empresa")
    private String nombre;

    @Column(columnDefinition = "TEXT")
    @Comment("Descripcion de la empresa")
    private String descripcion;

    @Comment("Nombre corto de la empresa")
    private String nombreCorto;

    @Comment("Url de la pagina de la empresa")
    @Column(columnDefinition = "TEXT")
    private String urlPagina;

    @Comment("Url del logo de la empresa")
    @Column(columnDefinition = "TEXT")
    private String urlLogo;

    @Comment("Id del nodo del alfresco asociado a una carpeta")
    private String idCarpetaAlfresco;

    @Comment("Id del nodo del alfresco asociado a una carpeta de notificaciones email")
    private String idCarpetaAlfrescoNotificacionesEmail;

    @Comment("Id del nodo del alfresco asociado a una carpeta de gestion del adolescente")
    private String idCarpetaAlfrescoGestionAdolescente;

    @Comment("Username de alfresco")
    private String userNameAlfresco;

    @Comment("Contrasenia de alfresco")
    private String constraseniaAlfresco;

    @Comment("Color principal de de la empresa")
    private String colorPrimarioHex;

    @Comment("Color secundario de la empresa")
    private String colorSecundarioHex;

    @Comment("Declara si los usuarios deben de cambiar la contraseña cada n días")
    private Boolean usuariosDebenDeCambiarContraseniaLuegoDeNDias = true;

    public EmpresaDTO convertirADTO() {
        EmpresaDTO empresaDTO = new EmpresaDTO();
        empresaDTO.setNombre(this.nombre);
        empresaDTO.setDescripcion(this.descripcion);
        empresaDTO.setIpCrea(this.getIpCrea());
        empresaDTO.setColorPrimarioHex(this.colorPrimarioHex);
        empresaDTO.setColorSecundarioHex(this.colorSecundarioHex);
        empresaDTO.setConstraseniaAlfresco(this.constraseniaAlfresco);
        empresaDTO.setNombreCorto(this.nombreCorto);
        empresaDTO.setUrlLogo(this.urlLogo);
        empresaDTO.setUserNameAlfresco(this.userNameAlfresco);
        empresaDTO.setFechaCreacion(
                this.getFechaCreacion()
        );
        empresaDTO.setTokenIdentificador(this.getTokenIdentificador());
        return empresaDTO;
    }

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
