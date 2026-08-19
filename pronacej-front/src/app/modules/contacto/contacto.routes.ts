import { Routes } from '@angular/router';
import { CrearContactoComponent } from './crear-contacto/crear-contacto.component';
import { ListarContactoComponent } from './listar-contacto/listar-contacto.component';
export default [
    {
        path: '',
        children: [
            {
                path: 'listado',
                component: ListarContactoComponent,
            },
            {
                path: "contacto-adolescente/crear-editar",
                component: CrearContactoComponent,
            },
            
        ],
    }
] as Routes;