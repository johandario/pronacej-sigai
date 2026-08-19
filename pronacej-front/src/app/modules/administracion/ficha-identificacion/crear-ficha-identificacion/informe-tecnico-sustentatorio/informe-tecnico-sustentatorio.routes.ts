    import { Routes } from '@angular/router';
    import { InformeTecnicoSustentatorioCrearEditarComponent } from './informe-tecnico-sustentatorio-crear-editar/informe-tecnico-sustentatorio-crear-editar.component';
    import { InformeTecnicoSustentatorioComponent } from './informe-tecnico-sustentatorio.component';

    export default [
        {
            path: '',
            children: [
                {
                    path: '',
                    component: InformeTecnicoSustentatorioComponent
                },
                {
                    path: 'crear-editar',
                    component: InformeTecnicoSustentatorioCrearEditarComponent
                },
                {
                    path: 'crear-editar/:uuid',
                    component: InformeTecnicoSustentatorioCrearEditarComponent
                }
            ]
        },
        
    ] as Routes;
