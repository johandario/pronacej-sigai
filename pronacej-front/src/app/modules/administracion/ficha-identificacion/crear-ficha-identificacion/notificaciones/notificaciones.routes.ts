import { Routes } from '@angular/router';
import { EnvioNotificacionComponent } from 'app/core/components/notificacion/envio-notificacion/envio-notificacion.component';
import { NotificacionesVerComponent } from 'app/core/components/notificacion/notificaciones-ver/notificaciones-ver.component';

export default [
    {
        path: '',
        children: [
            {
                path: '',
                component: NotificacionesVerComponent
            },
            {
                path: 'crear',
                component: EnvioNotificacionComponent
            },
            {
                path: 'ver',
                component: EnvioNotificacionComponent
            },
            {
                path: 'crear-editar/:uuid',
                component: EnvioNotificacionComponent
            }
        ]
    },

] as Routes;
