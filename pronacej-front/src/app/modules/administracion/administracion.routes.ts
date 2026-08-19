import { Routes } from '@angular/router';
import { ListarFichasIdentificacionComponent } from './ficha-identificacion/listar-fichas-identificacion/listar-fichas-identificacion.component';
import { CrearFichaIdentificacionComponent } from './ficha-identificacion/crear-ficha-identificacion/crear-ficha-identificacion.component';

export default [
    {
        path: '',
        children: [
            {
                path: 'ficha-identificacion',
                component: ListarFichasIdentificacionComponent,
            },
            {
                path: "ficha-identificacion/crear-editar",
                loadChildren: () => import("./ficha-identificacion/crear-ficha-identificacion/crear-ficha-identificacion.routes"),
            },
        ],
    }
] as Routes;
