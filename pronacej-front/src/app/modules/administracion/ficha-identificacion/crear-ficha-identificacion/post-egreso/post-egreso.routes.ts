import { Routes } from '@angular/router';
import { CrearEditarPlanAsistPostEgresoComponent } from '../plan-asistencia-post-egreso/crear-editar-plan-asist-post-egreso/crear-editar-plan-asist-post-egreso.component';
import { PostEgresoComponent } from './post-egreso.component';
import { CrearEditarInformeFinalAsistenciaComponent } from './informe-final-asistencia/crear-editar-informe-final-asistencia/crear-editar-informe-final-asistencia.component';
import { CrearEditarAsistenciaPostEgresoComponent } from './asistencia-seguimiento-post-egreso/crear-editar-asistencia-post-egreso/crear-editar-asistencia-post-egreso.component';
import { CrearContactoComponent } from 'app/modules/contacto/crear-contacto/crear-contacto.component';
import { GestAdolescenteDerivadoComponent } from 'app/modules/institucion/seguimiento-adolescente-inst/gest-adolescente-derivado/gest-adolescente-derivado.component';
import { CrearSeguimientoAdolescenteComponent } from 'app/modules/institucion/seguimiento-adolescente-inst/crear-seguimiento-adolescente/crear-seguimiento-adolescente.component';

export default [
    {
        path: '',
        children: [  
          {
            path: '',
            component: PostEgresoComponent
          },
          {
            path: 'crear-editar-plan-asistencia',
            component: CrearEditarPlanAsistPostEgresoComponent
          },
          {
            path: 'crear-editar-plan-asistencia/crear-editar-informe-final',
            component: CrearEditarInformeFinalAsistenciaComponent
          },
          {
            path: 'crear-editar-plan-asistencia/crear-editar-ficha-asistencia',
            component: CrearEditarAsistenciaPostEgresoComponent
          },
          {
            path: 'crear-editar-contacto',
            component: CrearContactoComponent
          },
          {
            path: 'crear-derivado-institucion',
            component: GestAdolescenteDerivadoComponent
          },
          {
            path: 'crear-editar-derivado-institucion/:uuid',
            component: GestAdolescenteDerivadoComponent
          },
          {
            path: 'crear-editar-derivado-institucion/:uuid/crear-editar-seguimiento-institucion',
            component: CrearSeguimientoAdolescenteComponent
          },
        ]
    },
    
] as Routes;
