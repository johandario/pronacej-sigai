package net.latinus.sistema.integral.gestion.seguridad.entities.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.EntidadBase;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.hibernate.annotations.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

@Entity
@Data
@Table(name = "seg_menu")
@EqualsAndHashCode(of = {"idMenu"}, callSuper = true)
@Comment("Tabla de menu")
public class Menu extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Id de menu")
    private Long idMenu;

    @Comment("Clave para permisos del menu")
    private String clave;
    
    @Comment("titulo del menu")
    private String titulo;

    @Comment("Subtitlo del menu")
    private String subtitulo;

    @Comment("Tipo de menu")
    private String tipo;  // disponibles por la plantilla del front 'aside' | 'basic' | 'collapsable' | 'divider' | 'group' | 'spacer'

    @Comment("Menu activo")
    private Boolean activo = true;

    @Comment("Menu desabilatado")
    private Boolean desabilitado = false;

    @Comment("Tooltip del menu")
    private String tooltip;

    @Comment("Tabla de menu")
    private String link;

    @Comment("Icono del menu")
    private String icono = "heroicons_outline:information-circle";

    @Comment("Nemonico del menu")
    private String nemonico;

    @Comment("Id del menu padre")
    @JoinColumn(name = "id_menu_padre", referencedColumnName = "idMenu")
    @ManyToOne(fetch = FetchType.LAZY)
    private Menu menuPadre;

    @Comment("Indica si el menu es padre")
    private Boolean esPadre = false;

    @Comment("Indica si se muestra el menu en el front")
    private Boolean mostrarEnElFront = true;

    @Comment("Indica si se realiza auditoria")
    private Boolean realizaAuditoria = true;

    @Comment("Indica si se muestra en el módulo de permisos")
    private Boolean mostrarEnPermisos = false;

    @Comment("Indica si se muestran acciones en el módulo de permisos")
    private Boolean mostrarAccionesPermisos = true;

    @Comment("Indica el orden")
    private Long orden;

    @Comment("Id de la empresa")
    @JoinColumn(name = "id_empresa", referencedColumnName = "idEmpresa")
    @ManyToOne(fetch = FetchType.LAZY)
    private Empresa empresa;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);

    }

    /*
        //datos iniciales
        [
    {
        "title": "Seguridad",
        "subtitile": "Opciones de seguridad de la aplicación",
        "type": "group",
        "icon": "heroicons_outline:adjustments-horizontal",
        "children": [
            {
                "title": "Sistema",
                "subtitile": null,
                "type": "collapsable",
                "icon": "heroicons_outline:square-3-stack-3d",
                "children": [
                    {
                        "title": "Usuarios",
                        "subtitile": null,
                        "type": "basic",
                        "icon": "heroicons_outline:user-circle",
                        "link": "/seguridad/sistema/usuario"
                    },
                    {
        "title": "Roles",
        "subtitile": null,
        "type": "basic",
        "icon": "heroicons_outline:user-circle",
        "link": "/seguridad/sistema/rol",
        "tokenIdentificadorPadre": "ac58844e-3f5e-4324-8adf-47f26829168a"
    },
    {
        "title": "Menu roles",
        "subtitile": null,
        "type": "basic",
        "icon": "heroicons_outline:user-circle",
        "link": "/seguridad/sistema/menu-rol",
        "tokenIdentificadorPadre": "ac58844e-3f5e-4324-8adf-47f26829168a"
    },
     {
        "title": "Auditorias sistema",
        "subtitile": null,
        "type": "basic",
        "icon": "heroicons_outline:user-circle",
        "link": "/seguridad/sistema/auditorias-sistema",
        "tokenIdentificadorPadre": "ac58844e-3f5e-4324-8adf-47f26829168a"
    }
                ]
            }
        ]
    }
]


//Menu de autenticación
[
    {
        "title": "Autenticacion",
        "subtitile": null,
        "type": "group",
        "icon": "heroicons_outline:user-circle",
        "link": null,
        "tokenIdentificadorPadre": null,
        "mostrarEnFront": false,
        "nemonico": "MENU_AUTENTICACION",
                "tokenIdentificadorEmpresa": "459a4dd4-eaf8-40c3-99ac-619dbf154405"
        "children":[
        {
        "title": "Login usuario",
        "subtitile": null,
        "type": "basic",
        "nemonico": "MENU_AUTH_LOGIN_USUARIO",
        "icon": "heroicons_outline:user-circle",
        "link": null,
        "tokenIdentificadorPadre": "a8296ed7-e0c9-4c87-8690-b3ebd987cdfd",
        "mostrarEnFront": false,
        "tokenIdentificadorEmpresa": "459a4dd4-eaf8-40c3-99ac-619dbf154405"
    }
        ]
    }
]

     */
}
