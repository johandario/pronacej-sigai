import { Routes } from '@angular/router';
import { CrearFichaIdentificacionComponent } from './crear-ficha-identificacion.component';
import { DatosGeneralesComponent } from './datos-generales/datos-generales.component';
import { FichaDocumentosComponent } from './ficha-documentos/ficha-documentos.component';
import { RegistroSalidaComponent } from 'app/modules/salida/registro-salida/registro-salida.component';
import { PermisosMenuGuard } from 'app/core/guards/permisos-menu.guard';
import { FichaResolver } from 'app/core/resolvers/ficha-identificacion.resolver';

export default [
    {
        path: '',
        component: CrearFichaIdentificacionComponent, //DRAWER
        //canActivateChild: [PermisosMenuGuard],
        // resolve: { ficha: FichaResolver },
        children: [
            {
                //MENU_DOCUMENTOS
                path: "idPanelDocumentacion/:uuid_fp",
                loadChildren: ()=> import("./ficha-documentos/ficha-documentos.routes")
            },
            {
                //MENU_FICHA_PRINCIPAL
                path: 'fichaPrincipal',
                component: DatosGeneralesComponent,
            },
            {
                path: "fichaPrincipal/:uuid_fp",
                component: DatosGeneralesComponent
            },
            {
                //MENU_FICHA_INGRESO
                path: 'fichaDeIngreso',
                resolve: { ficha: FichaResolver },
                // data: { nemonicoMenu: etiquetasModel.NEMONICO_MENU_FICHA_INGRESO },
                loadChildren: () =>
                    import('./ficha-ingreso/ficha-ingreso.routes'),
            },
            {
                path: "fichaDeIngreso/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./ficha-ingreso/ficha-ingreso.routes")
            },
            {
                //MENU_VALORACION_DE_NIVEL_DE_RIESGO
                path: "nivelDeRiesgo",
                loadChildren: () => import("./nivel-riesgo/nivel-riesgo.routes")
            },
            {
                path: "nivelDeRiesgo/:uuid_fp",
                loadChildren: () => import("./nivel-riesgo/nivel-riesgo.routes")
            },
            {
                //MENU_EXPEDIENTES_LEGALES
                path: "expediente",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./expediente-matriz/expediente-matriz.routes")
            },
            {
                path: "expediente/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./expediente-matriz/expediente-matriz.routes")
            },
            {
                //MENU_FICHA_PSICOSOCIAL
                path: "fichaPsicosocial",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./ficha-psicosocial/ficha-psicosocial.routes")
            },
            {
                path: "fichaPsicosocial/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./ficha-psicosocial/ficha-psicosocial.routes")
            },
            {
                path: "fichaUbicacion/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./ficha-ubicacion/ficha-ubicacion.routes")
            },
            {
                //MENU_EVALUACIONES_PSICOLOGICAS
                path: "evaluacionPsicologica",
                loadChildren: () => import("./evaluacion-psicologica/evaluacion-psicologica.routes")
            },
            {
                path: "evaluacionPsicologica/:uuid_fp",
                loadChildren: () => import("./evaluacion-psicologica/evaluacion-psicologica.routes")
            },
            {
                //MENU_EVALUACION_DE_SALUD
                path: "evaluacionMedica",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./evaluacion-medica/evaluacion-medica.routes")
            },
            {
                path: "evaluacionMedica/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./evaluacion-medica/evaluacion-medica.routes")
            },
            {
                //MENU_ENTREGA_RETIRO_DE_PERTENENCIAS
                path: 'entregaRecepcionUniformesArticulos',
                resolve: { ficha: FichaResolver },
                loadChildren: () =>
                    import(
                        './entrega-recepcion-uniformes-articulos/entrega-recepcion-uniformes-articulos.routes'
                    ),
            },
            {
                path: 'entregaRecepcionUniformesArticulos/:uuid_fp',
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./entrega-recepcion-uniformes-articulos/entrega-recepcion-uniformes-articulos.routes")
            },
            {
                //MENU_PLAN_DE_TRATAMIENTO_INDIVIDUAL
                path: "planTratamiento",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./plan-tratamiento/plan-tratamiento.routes")
            },
            {
                path: "planTratamiento/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./plan-tratamiento/plan-tratamiento.routes")
            },
            {
                path: "fichaPrincipal/:uuid_fp/datos-personales",
                loadChildren: () => import("./datos-familiares/datos-familiares.routes")
            },
            {
                //MENU_INFORMES
                path: "informes",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./informes/informes.routes")
            },
            {
                path: "informes/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./informes/informes.routes")
            },
            {
                //MENU_PREPARACION_PARA_EL_EGRESO
                path: "preparacionEgreso",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./preparacion-egreso/preparacion-egreso.routes")
            },
            {
                path: "preparacionEgreso/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./preparacion-egreso/preparacion-egreso.routes")
            },
            {
                //MENU_NOTIFICACIONES
                path: "notificaciones",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./notificaciones/notificaciones.routes")
            },
            {
                path: "notificaciones/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./notificaciones/notificaciones.routes")
            },
            {
                //MENU_PERMISO_DE_SALIDA
                path: "salidas/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./fuga/fuga.routes")
            },
            {
                path: "salidas",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./fuga/fuga.routes")
            },

            {
                //MENU_EVALUACION_CONDUCTUAL
                path: "evaluacionConductual/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./evaluacion-conductual/evaluacion-conductual.routes")
            },
            {
                //MENU_EVALUACION_SOCIAL
                path: "evaluacionSocial/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./evaluacion-social/evaluacion-social.routes")
            },
            {
                //MENU_PROGRAMA_DE_INTERVENCION_INTENSIVA
                path: "programaIntervencionRepentina/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./programa-intervencion-intensiva/programa-intervencion-intensiva.routes")
            },
            {
                path: "informeSeguimiento/:uuid_fp",
                loadChildren: () => import("./informe-seguimiento/informe-seguimiento.routes")
            },
            {
                path: "informeEgresoPII/:uuid_fp",
                loadChildren: () => import("./informe-egreso-pii/informe-egreso-pii.routes")
            },
            {
                //MENU_INFORME_FINAL
                path: "informeFinal",
                loadChildren: () => import("./informe-final/informe-final.routes")
            },
            {
                path: "informeFinal/:uuid_fp",
                loadChildren: () => import("./informe-final/informe-final.routes")
            },
            {
                //MENU_EVALUACION_DE_SALUD
                path: "historiaClinica",
                loadChildren: () => import("./historia-clinica/historia-clinica.routes")
            },
            {
                path: "historiaClinica/:uuid_fp",
                loadChildren: () => import("./historia-clinica/historia-clinica.routes")
            },
            {
                //MENU_POST_EGRESO
                path: "postEgreso",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./post-egreso/post-egreso.routes")
            },
            {
                path: "postEgreso/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./post-egreso/post-egreso.routes")
            },
            {
                //MENU_PERMISO_DE_SALIDA
                path: "salidas",
                loadChildren: () => import("./salidas/salidas.routes")
            },
            {                
                path: "contacto/:uuid_fp",
                loadChildren: () => import("./contacto-adolescente-gestion/contacto-adolescente.routes")
            },
            {
                path: "contacto",
                loadChildren: () => import("./contacto-adolescente-gestion/contacto-adolescente.routes")
            },
            {
                //MENU_SALIDAS_FINALIZADAS
                path: 'trasladosFinalizados/:uuid_fp',
                loadChildren: () =>
                    import('./salidas-traslados/salidas-traslados.routes'),
            },
            {
                //MENU_SANCIONES_DISCIPLINARIAS
                path: "sancionesDisciplinarias/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./sanciones-disciplinarias/sanciones-disciplinarias.routes")
            },
            {
                path: "sancionesDisciplinarias",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./sanciones-disciplinarias/sanciones-disciplinarias.routes")
            },
            {
                //MENU_TRABAJO_LABORAL
                path: "trabajoLaboral",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./trabajo-laboral/trabajo-laboral.routes")
            },
            {
                path: "trabajoLaboral/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./trabajo-laboral/trabajo-laboral.routes")
            },
            {
                //MENU_ESTUDIOS
                path: "estudios",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./estudios/estudios.routes")
            },
            {
                path: "estudios/:uuid_fp",
                resolve: { ficha: FichaResolver },
                loadChildren: () => import("./estudios/estudios.routes")
            },

        ],
    },
] as Routes;
