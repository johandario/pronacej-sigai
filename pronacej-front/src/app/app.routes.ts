import { Route } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { NoAuthGuard } from 'app/core/auth/guards/noAuth.guard';
import { LayoutComponent } from 'app/layout/layout.component';
import { HomeComponent } from './modules/home/home.component';
import { MantenimientoComponent } from './modules/admin/paginas/mantenimiento/mantenimiento.component';
import { SinPermisosComponent } from './modules/admin/paginas/sin-permisos/sin-permisos.component';
import { FaltaVerificacionComponent } from './modules/admin/paginas/falta-verificacion/falta-verificacion.component';

// @formatter:off
/* eslint-disable max-len */
/* eslint-disable @typescript-eslint/explicit-function-return-type */
export const appRoutes: Route[] = [

    // Redirect empty path to '/example'
    { path: '', pathMatch: 'full', redirectTo: 'home' },

    // Redirect signed-in user to the '/example'
    //
    // After the user signs in, the sign-in page will redirect the user to the 'signed-in-redirect'
    // path. Below is another redirection for that path to redirect the user to the desired
    // location. This is a small convenience to keep all main routes together here on this file.
    { path: 'signed-in-redirect', pathMatch: 'full', redirectTo: 'home' },

    //Errores o mantenimiento
    { path: "app-en-mantenimiento", component: MantenimientoComponent },
    { path: "sin-permisos", component: SinPermisosComponent },
    { path: "falta-verificacion", component: FaltaVerificacionComponent },

    // Auth routes for guests
    {
        path: '',
        canActivate: [NoAuthGuard],
        canActivateChild: [NoAuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            { path: 'confirmation-required', loadChildren: () => import('app/modules/auth/confirmation-required/confirmation-required.routes') },
            { path: 'olvido-de-contrasenia', loadChildren: () => import('app/modules/auth/forgot-password/forgot-password.routes') },
            { path: 'reset-password', loadChildren: () => import('app/modules/auth/reset-password/reset-password.routes') },
            { path: 'sign-in', loadChildren: () => import('app/modules/auth/sign-in/sign-in.routes') },
            { path: 'sign-up', loadChildren: () => import('app/modules/auth/sign-up/sign-up.routes') }
        ]
    },

    // Auth routes for authenticated users
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            { path: 'sign-out', loadChildren: () => import('app/modules/auth/sign-out/sign-out.routes') },
            { path: 'unlock-session', loadChildren: () => import('app/modules/auth/unlock-session/unlock-session.routes') }
        ]
    },

    // Landing routes
    /*
    {
        path: '',
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            { path: 'home', loadChildren: () => import('app/modules/landing/home/home.routes') },
        ]
    },
    */

    // Admin routes
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: {
            initialData: initialDataResolver
        },
        children: [
            //path de ejemplo
            //{ path: 'example', loadChildren: () => import('app/modules/admin/example/example.routes') },

            { path: "home", component: HomeComponent },

            //Seguridad
            { path: "seguridad", loadChildren: () => import("app/modules/seguridad/sistema/sistema.routes") },

            //Gestion adolescente
            { path: "gestion-adolescente", loadChildren: () => import("app/modules/administracion/administracion.routes") },

            //Flujos
            { path: "flujos", loadChildren: () => import("app/modules/flujos/flujos.routes") },

            //General
            { path: "general", loadChildren: () => import("app/modules/general/general.routes") },

            //Flujo trabajo
            { path: "flujo-trabajo", loadChildren: () => import("app/modules/flujo-trabajo/flujo-trabajo.routes") },


            //Configuracion
            { path: "configuracion", loadChildren: () => import("app/modules/configuracion/configuracion.routes") },

            //Notificacion
            {
                path: "notificacion",
                loadChildren: () => import("app/modules/notificacion/notificacion.route")
            },

            //Registro Ingreso
            {
                path: "registro-ingreso",
                loadChildren: () => import("app/modules/registro-ingreso/registro-ingreso.routes")
            },
            //Salidas
            { path: "salida", loadChildren: () => import("app/modules/salida/salida.routes") },

            //Registro de institucion
            { path: "institucion", loadChildren: () => import("app/modules/institucion/institucion.routes") },

            //Contactos adolescente
            { path: "contacto", loadChildren: () => import("app/modules/contacto/contacto.routes") },

            //Registro Estadístico
            { path: 'reportes', loadChildren: () => import("app/modules/reportes/reportes.routes") },

            //Dashboard
            { path: 'dashboard', loadChildren: () => import("app/modules/dashboard/dashboard.routes") },
        ]
    },

    
];
