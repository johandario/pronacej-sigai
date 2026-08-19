import { Routes } from "@angular/router";
import { TFAdministrarComponent } from "./tipo-de-archivos-ficha-principal/t-f-administrar/t-f-administrar.component";
import { TFInformacionComponent } from "./tipo-de-archivos-ficha-principal/t-f-informacion/t-f-informacion.component";

export default [
    {
        path: "tipo-de-documento-seccion-ficha-principal",
        component: TFAdministrarComponent,
        children: [
            {
                path: ":tokenSeccion",
                component: TFInformacionComponent
            }
        ]
    },
    {
        path: 'ubicacion',
        loadChildren: () => import('app/modules/ubicacion/ubicacion.routes'),
    }
] as Routes;