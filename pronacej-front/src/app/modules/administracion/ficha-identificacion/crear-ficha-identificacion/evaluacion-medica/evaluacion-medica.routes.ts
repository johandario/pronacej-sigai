import { Routes } from '@angular/router';
import { EvaluacionMedicaComponent } from './evaluacion-medica.component';
import { CrearEvaluacionMedicaComponent } from './crear-evaluacion-medica/crear-evaluacion-medica.component';
import { SeguimientoEvaluacionMedicaComponent } from './seguimiento-evaluacion-medica/seguimiento-evaluacion-medica.component';
import { VerEvaluacionMedicaComponent } from './ver-evaluacion-medica/ver-evaluacion-medica.component';


export default [
    {
        path: "",
        children: [
            {
                path: "",
                component: EvaluacionMedicaComponent,
                children: [
                    { path: 'seguimiento', component: SeguimientoEvaluacionMedicaComponent, },
                    { path: 'crear', component: CrearEvaluacionMedicaComponent },
                    { path: 'visualizar', component: CrearEvaluacionMedicaComponent },
                    { path: '', redirectTo: 'seguimiento', pathMatch: 'full' }
                ]
            },
               
        ]
    },
] as Routes;