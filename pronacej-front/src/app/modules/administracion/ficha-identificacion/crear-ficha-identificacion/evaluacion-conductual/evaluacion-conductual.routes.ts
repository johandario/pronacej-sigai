import { Routes } from "@angular/router";
import { EvaluacionComponent } from "app/core/components/evaluacion/evaluacion.component";
import { EvaluacionConductualVerComponent } from "./evaluacion-conductual-ver/evaluacion-conductual-ver.component";
import { SeguimientoConductualCrearEditarComponent } from "./seguimiento-conductual-crear-editar/seguimiento-conductual-crear-editar.component";
import { ActividadOcupacionalCrearEditarComponent } from "./actividad-ocupacional-crear-editar/actividad-ocupacional-crear-editar.component";
import { EvalSeguCrearEditarComponent } from "../eval-segu-educ-labo/eval-segu-crear-editar/eval-segu-crear-editar.component";
import { EvaluacionDocumentoComponent } from "app/modules/general/evaluacion-documento/evaluacion-documento.component";
import { SeguimientoConductualVerComponent } from "./seguimiento-conductual-ver/seguimiento-conductual-ver.component";

const routers: Routes = [
    {
        path: "",
        component: EvaluacionConductualVerComponent
    },
    {
        path: 'crear-editar',
        component: EvaluacionComponent
    },
    {
        path: 'crear-editar/:uuid',
        component: EvaluacionComponent
    },
    {
        path: 'seguimiento',
        component: SeguimientoConductualVerComponent
    },
    {
        path: 'seguimiento/crear-editar',
        component: SeguimientoConductualCrearEditarComponent
    },
    {
        path: 'seguimiento/crear-editar/:uuid',
        component: SeguimientoConductualCrearEditarComponent
    },
    {
        path: 'actividad-ocupacional/crear-editar',
        component: ActividadOcupacionalCrearEditarComponent
    },
    {
        path: 'actividad-ocupacional/crear-editar/:uuid',
        component: ActividadOcupacionalCrearEditarComponent
    },
    {
        path: 'evaluacion-seguimiento-social/crear-editar',
        component: EvalSeguCrearEditarComponent
    },
    {
        path: 'evaluacion-seguimiento-social/crear-editar/:uuid',
        component: EvalSeguCrearEditarComponent
    },
]

export default routers;