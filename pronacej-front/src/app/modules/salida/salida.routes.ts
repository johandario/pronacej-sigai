import { Routes } from '@angular/router';
import { RegistroSalidaComponent } from './registro-salida/registro-salida.component';
import { CrearEditarRegistroSalidaComponent } from './registro-salida/crear-editar-registro-salida/crear-editar-registro-salida.component';
import { ListarSalidaPermisoComponent } from './salida-permiso/listar-salida-permiso/listar-salida-permiso.component';
import { CrearPermisoSalidaComponent } from './salida-permiso/crear-permiso-salida/crear-permiso-salida.component';

export default [
    {
        path: '',
        children: [                      
            {
                path: 'salida-permiso',
                component: RegistroSalidaComponent,
            },
            {
                path: 'permiso-salida/crear-editar',
                component: CrearEditarRegistroSalidaComponent,
            },
            {
                path: 'registro-salida',
                component: ListarSalidaPermisoComponent,
            },
            {
                path: 'registro-salida/crear-editar',
                component: CrearPermisoSalidaComponent,
            },
           
        ],
    },
] as Routes;
