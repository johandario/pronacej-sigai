import { Routes } from '@angular/router';
import { RegistroSalidaComponent } from 'app/modules/salida/registro-salida/registro-salida.component';
import { CrearEditarRegistroSalidaComponent } from 'app/modules/salida/registro-salida/crear-editar-registro-salida/crear-editar-registro-salida.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: RegistroSalidaComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: CrearEditarRegistroSalidaComponent
            },
            {
                path: ':uuid/crear',
                component: CrearEditarRegistroSalidaComponent
            },
            {
                path: 'ver/:uuid',
                component: CrearEditarRegistroSalidaComponent
              }
              
            
        ]
    },
    
] as Routes;
