import { Routes } from '@angular/router';
import { CrearContactoComponent } from 'app/modules/contacto/crear-contacto/crear-contacto.component';
import { ListarContactoComponent } from 'app/modules/contacto/listar-contacto/listar-contacto.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: ListarContactoComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: CrearContactoComponent
            },
            {
                path: ':uuid/crear',
                component: CrearContactoComponent
            },
            {
                path: 'ver/:uuid',
                component: CrearContactoComponent
              }
              
            
        ]
    },
    
] as Routes;
