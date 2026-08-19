import { Routes } from '@angular/router';
import { CrearEditarMenuPermisoComponent } from './crear-editar-menu-permiso/crear-editar-menu-permiso.component';
import { ListaMenuPermisoComponent } from './lista-menu-permiso/lista-menu-permiso.component';

export default [
    {
        path: '',
        component: ListaMenuPermisoComponent,
    },
    {
        path: 'crear',
        component: CrearEditarMenuPermisoComponent,
    },
    {
        path: 'editar/:token',
        component: CrearEditarMenuPermisoComponent,
    },
] as Routes;
