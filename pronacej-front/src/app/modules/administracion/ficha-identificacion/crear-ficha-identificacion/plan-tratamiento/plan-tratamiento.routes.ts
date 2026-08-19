import { Routes } from '@angular/router';
import { PlanTratamientoComponent } from './plan-tratamiento.component';
import { CrearEditarPlanTratamientoComponent } from './crear-editar/crear-editar-plan-tratamiento.component';
import { RegistroActividadPlanComponent } from './registro-actividad-plan/registro-actividad-plan.component';
import { ReajustePtiComponent } from './reajuste-pti/reajuste-pti.component';
import { ConsolidarIntervencionComponent } from './consolidar-intervencion/consolidar-intervencion.component';
import { ActividadDiferenciadaSeguimientoComponent } from './actividad-diferenciada-seguimiento/actividad-diferenciada-seguimiento.component';
import { SegPtiCerradoComponent } from './seguimiento-pti/seg-pti-cerrado/seg-pti-cerrado.component';
import { SubactividadesPtiComponent } from './subactividades-pti/subactividades-pti.component';
import { FichaSeguimientoIntervAbiertoComponent } from './crear-editar-pti-abierto/ficha-seguimiento-interv-abierto/ficha-seguimiento-interv-abierto.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: PlanTratamientoComponent
            },
            {
                path: 'crear-editar',
                component: CrearEditarPlanTratamientoComponent
            },
            {
                path: 'registro-actividad',
                component: RegistroActividadPlanComponent
            },
            {
                path: 'reajuste',
                component: ReajustePtiComponent
            }
            ,
            {
                path: 'consolidar-intervencion',
                component: ConsolidarIntervencionComponent
            },
            {
                path: 'seguimiento/crear-editar-cerrado',
                component: SegPtiCerradoComponent
            }
            ,
            {
                path: 'seguimiento-actividades',
                component: ActividadDiferenciadaSeguimientoComponent
            },
            {
                path: 'subactividades',
                component: SubactividadesPtiComponent
            },
            {
                path: 'ficha-seguimiento-abierto',
                component: FichaSeguimientoIntervAbiertoComponent
            }
        ]
    },
    
] as Routes;
