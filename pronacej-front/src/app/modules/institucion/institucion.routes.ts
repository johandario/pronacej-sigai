import { Routes } from '@angular/router';
import { CrearRegistroInstitucionComponent } from './registro-institucion/crear-registro-institucion/crear-registro-institucion.component';
import { ListarRegistroInstitucionComponent } from './registro-institucion/listar-registro-institucion/listar-registro-institucion.component';
import { CrearGestionInstitucionComponent } from './gestion-institucion/crear-gestion-institucion/crear-gestion-institucion.component';

export default [
    {
        path: '',
        children: [
            {
                path: 'registros',
                component: ListarRegistroInstitucionComponent,
            },
            {
                path: "registro-institucion/crear",
                component: CrearRegistroInstitucionComponent,
            },
            {
                path: "registro-institucion/crear-editar/:uuid",
                component: CrearRegistroInstitucionComponent,
            },
            {
                path: "registro-institucion/crear-gestion",
                component: CrearGestionInstitucionComponent,
            },

        ],
    }
] as Routes;