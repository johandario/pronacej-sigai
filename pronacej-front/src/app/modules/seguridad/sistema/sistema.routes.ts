import { Routes } from '@angular/router';
import { AuditoriaSistemaCompComponent } from './auditoria-sistema/auditoria-sistema-comp/auditoria-sistema-comp.component';
import { PerfilComponent } from './usuario/perfil/perfil.component';
import { JerarquiaComponent } from './jerarquia/jerarquia.component';
import { UsuariosVerComponent } from './usuarios/usuarios-ver/usuarios-ver.component';
import { UsuariosCrearEditarComponent } from './usuarios/usuarios-crear-editar/usuarios-crear-editar.component';
import { SubirArchivosEjemploComponent } from './subir-archivos-ejemplo/subir-archivos-ejemplo.component';
import { FuncionariosVerComponent } from './funcionarios/funcionarios-ver/funcionarios-ver.component';
import { FuncionariosCrearEditarComponent } from './funcionarios/funcionarios-crear-editar/funcionarios-crear-editar.component';
import { RolesVerComponent } from './roles/roles-ver/roles-ver.component';
import { RolesCrearEditarComponent } from './roles/roles-crear-editar/roles-crear-editar.component';
import { CargosVerComponent } from './cargos/cargos-ver/cargos-ver.component';
import { CargosCrearEditarComponent } from './cargos/cargos-crear-editar/cargos-crear-editar.component';

export default [
    {
        path: 'sistema',
        children: [
            {
                path: "usuario",
                loadChildren: () => import("./usuario/usuario.routes")
            },
            {
                path: "usuario-perfil",
                component: PerfilComponent
            },
            {
                path: "menu",
                loadChildren: () => import("./menu/menu.routes")
            },
            {
                path: "auditorias-sistema",
                component: AuditoriaSistemaCompComponent
            },
            {
                path: "rol",
                loadChildren: () => import("./rol/rol.routes")
            },
            {
                path: "menu-permiso",
                loadChildren: () => import("./menu-permiso/menu-permiso.routes")
            },
            {
                path: "menu-rol",
                loadChildren: () => import("./menu-rol/menu-rol.routes")
            },
            {
                path: "jerarquia",
                component: JerarquiaComponent
            },
            {
                path: "usuarios",
                component: UsuariosVerComponent
            },
            {
                path: "usuarios/crear",
                component: UsuariosCrearEditarComponent
            },
            {
                path: "usuarios/editar",
                component: UsuariosCrearEditarComponent
            },
            {
                path: "funcionarios",
                component: FuncionariosVerComponent
            },
            {
                path: "funcionarios/crear",
                component: FuncionariosCrearEditarComponent
            },
            {
                path: "funcionarios/editar",
                component: FuncionariosCrearEditarComponent
            },
            {
                path: "roles",
                component: RolesVerComponent
            },
            {
                path: "roles/crear",
                component: RolesCrearEditarComponent
            },
            {
                path: "roles/editar",
                component: RolesCrearEditarComponent
            },
            {
                path: "cargos",
                component: CargosVerComponent
            },
            {
                path: "cargos/crear",
                component: CargosCrearEditarComponent
            },
            {
                path: "cargos/editar",
                component: CargosCrearEditarComponent
            },
            {
                path: "funcionario",
                loadChildren: () => import("./funcionario/funcionario.routes")
            },
            {
                path: "prueba-doc",
                component: SubirArchivosEjemploComponent
            },

            //Catalogo
            { path: "catalogos", loadChildren: () => import("app/modules/catalogo/catalogo.routes") },
            {
                path: "institucion",
                loadChildren: () => import("../../institucion/institucion.routes")
            },
            //localidades
            { path: "localidades", loadChildren: () => import("app/modules/localidades/localidades.routes") },
        ],
    }
] as Routes;
