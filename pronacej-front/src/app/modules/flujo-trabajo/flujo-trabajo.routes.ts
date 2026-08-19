
import { Routes } from '@angular/router';
import { FlujoProcesoComponent } from './flujo-proceso/flujo-proceso.component';
import { CrearEditarProcesoComponent } from './flujo-proceso/crear-editar-proceso/crear-editar-proceso.component';
import { BandejaEntradaFlujoComponent } from './bandeja-entrada-flujo/bandeja-entrada-flujo.component';
import { BandejaSalidaFlujoComponent } from './bandeja-salida-flujo/bandeja-salida-flujo.component';
import { FlujoInstanciaComponent } from './flujo-instancia/flujo-instancia.component';
import { TrasladoAnalistaComponent } from './traslado/traslado-analista/traslado-analista.component';
import { TrasladoDirectorComponent } from './traslado/traslado-director/traslado-director.component';
import { FugaAnalistaComponent } from './gestion-fuga/fuga-analista/fuga-analista.component';
import { FugaDirectorComponent } from './gestion-fuga/fuga-director/fuga-director.component';
import { FugaApoderadoComponent } from './gestion-fuga/fuga-apoderado/fuga-apoderado.component';
import { TrasladoDirectorAprobacionComponent } from './traslado/traslado-director-aprobacion/traslado-director-aprobacion.component';
import { RelacionAdolescentesEgresoComponent } from './relacion-adolescente-egreso/relacion-adolescente-egreso.component';
import { TrasladoComponent } from './traslado/traslado.component';
import { BandejaBorradoresFlujoComponent } from './bandeja-borradores-flujo/bandeja-borradores-flujo.component';

export default [
    {
        path: '',
        children: [                      
            {
                path: 'admin-procesos',
                component: FlujoProcesoComponent,
            },
            {
                path: 'admin-procesos/crear-editar',
                component: CrearEditarProcesoComponent,
            },
            {
                path: 'bandeja-entrada',
                component: BandejaEntradaFlujoComponent,
            },
            {
                path: 'bandeja-salida',
                component: BandejaSalidaFlujoComponent,
            },
            {
                path: 'bandeja-borrador',
                component: BandejaBorradoresFlujoComponent,
            },
            {
                path: 'flujo-instancia',
                component: FlujoInstanciaComponent
            },
            {
                path: 'traslado',
                component: TrasladoComponent,
                children: [
                    {
                        path: 'traslado-analista',
                        component: TrasladoAnalistaComponent
                    },
                    {
                        path: 'traslado-analista/:tokenID',
                        component: TrasladoAnalistaComponent
                    },
                    {
                        path: 'traslado-director',
                        component: TrasladoDirectorComponent
                    },
                    {
                        path: 'traslado-director/:tokenID',
                        component: TrasladoDirectorComponent
                    },
                    {
                        path: 'traslado-director-aprobacion',
                        component: TrasladoDirectorAprobacionComponent
                    },
                    {
                        path: 'traslado-director-aprobacion/:tokenID',
                        component: TrasladoDirectorAprobacionComponent
                    },
                ]
            },
            {
                path: 'fuga',
                component: TrasladoComponent,
                children: [
                    {
                        path: 'fuga-jefe-seguridad',
                        component: FugaAnalistaComponent
                    },
                    {
                        path: 'fuga-jefe-seguridad/:tokenID',
                        component: FugaAnalistaComponent
                    },
                    {
                        path: 'fuga-director',
                        component: FugaDirectorComponent
                    },
                    {
                        path: 'fuga-director/:tokenID',
                        component: FugaDirectorComponent
                    },
                    {
                        path: 'fuga-director-general',
                        component: FugaApoderadoComponent
                    },
                    {
                        path: 'fuga-director-general/:tokenID',
                        component: FugaApoderadoComponent
                    },
                    
                ]
            },


            // {
            //     path: 'traslado/traslado-analista',
            //     component: TrasladoAnalistaComponent
            // },
            // {
            //     path: 'traslado/traslado-analista/:tokenID',
            //     component: TrasladoAnalistaComponent
            // },
            // {
            //     path: 'traslado/traslado-director',
            //     component: TrasladoDirectorComponent
            // },
            // {
            //     path: 'traslado/traslado-director/:tokenID',
            //     component: TrasladoDirectorComponent
            // },
            // {
            //     path: 'traslado/traslado-director-aprobacion',
            //     component: TrasladoDirectorAprobacionComponent
            // },
            // {
            //     path: 'traslado/traslado-director-aprobacion/:tokenID',
            //     component: TrasladoDirectorAprobacionComponent
            // },
            // {
            //     path: 'fuga/fuga-jefe-seguridad',
            //     component: FugaAnalistaComponent
            // },
            // {
            //     path: 'fuga/fuga-jefe-seguridad/:tokenID',
            //     component: FugaAnalistaComponent
            // },
            // {
            //     path: 'fuga/fuga-director',
            //     component: FugaDirectorComponent
            // },
            // {
            //     path: 'fuga/fuga-director/:tokenID',
            //     component: FugaDirectorComponent
            // },
            // {
            //     path: 'fuga/fuga-director-general',
            //     component: FugaApoderadoComponent
            // },
            // {
            //     path: 'fuga/fuga-director-general/:tokenID',
            //     component: FugaApoderadoComponent
            // },
            {
                path: 'relacion-adolescente-egreso',
                component: RelacionAdolescentesEgresoComponent,
            },
        ],
    },
] as Routes;
