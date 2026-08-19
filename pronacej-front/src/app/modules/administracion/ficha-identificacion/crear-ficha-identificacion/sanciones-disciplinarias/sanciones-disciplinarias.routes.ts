import { Routes } from '@angular/router';
import { SancionesDisciplinariasComponent } from './sanciones-disciplinarias.component';
import { CrearSancionesDiscComponent } from './crear-sanciones-disc/crear-sanciones-disc.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: SancionesDisciplinariasComponent
            },
            {
                path: ':uuid/crear',
                component: CrearSancionesDiscComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: CrearSancionesDiscComponent
            },
{
                path: 'ver/:uuid',
                component: CrearSancionesDiscComponent
            }
        

        ]
    },

] as Routes;