import { Routes } from '@angular/router';
import { CrearFichaIdentificacionComponent } from './crear-ficha-identificacion/crear-ficha-identificacion.component';
import { CrearFuncionarioUsuarioComponent } from './crear-funcionario-usuario/crear-funcionario-usuario.component';

export default [
    {
        path: '',
        children: [
            {
                path: "crear-ficha-identificacion",
                component: CrearFichaIdentificacionComponent
            },
            {
                path: "crear-funcionario-usuario",
                component: CrearFuncionarioUsuarioComponent
            },
            // {
            //     path: "fichaDeIdentificacion/administrar/crear",
            //     loadChildren: () => import("./ficha-identificacion/crear-ficha-identificacion/crear-ficha-identificacion.routes")
            // },
            // {
            //     path: "fichaDeIdentificacion/administrar/crear/fichaDeIngreso",
            //     loadChildren: () => import("./ficha-identificacion/crear-ficha-identificacion/ficha-ingreso/ficha-ingreso.routes")
            // }
        ],
    }
] as Routes;
