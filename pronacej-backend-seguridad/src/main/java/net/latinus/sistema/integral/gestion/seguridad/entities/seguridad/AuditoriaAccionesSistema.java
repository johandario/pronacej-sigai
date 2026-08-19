package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Data
@Table(name = "seg_auditoria_acciones_sistema")
@EqualsAndHashCode(of = {"idAuditoriaAccionesSistema"}, callSuper = true)
@Comment("Tabla de auditorias de acciones del sistema")
public class AuditoriaAccionesSistema extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de auditorias de acciones del sistema")
    private Long idAuditoriaAccionesSistema;

    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la empresa")
    private Empresa empresa;

    @JoinColumn(name = "id_usuario_que_realiza_la_accion", referencedColumnName = "idUsuarioSistema")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del usuario que realiza la operación")
    private UsuarioSistema usuarioQueRealizaLaAccion;

    @JoinColumn(name = "id_rol", referencedColumnName = "idRol")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del rol que realiza la operación")
    private Rol rol;

    @JoinColumn(name = "id_accion", referencedColumnName = "idCatalogo")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id de la accion que se realiza")
    private Catalogo accion;

    @JoinColumn(name = "id_auditoria_servicio_rest", referencedColumnName = "idAuditoriaServicioRest")
    @OneToOne(fetch = FetchType.LAZY)
    @Comment("Id del servicio rest que se consume")
    private AuditoriaServicioRest auditoriaServicioRest;

    @Comment("Fecha inicio de la acción")
    private Date fechaInicioAccion;

    @Comment("Fecha fin de la acción")
    private Date fechaFinAccion;

    @JoinColumn(name = "id_menu", referencedColumnName = "idMenu")
    @ManyToOne(fetch = FetchType.LAZY)
    @Comment("Id del menu donde se realiza la acción")
    private Menu menu;

    @Column(columnDefinition = "TEXT")
    @Comment("Json response del recapthca v3")
    private String jsonResponseRecaptchaV3;

    @Column(columnDefinition = "TEXT")
    @Comment("Descripción de la acción realizada")
    private String descripcion;


    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
