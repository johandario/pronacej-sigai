import { Routes } from "@angular/router";
import { FichaDocumentosComponent } from "./ficha-documentos.component";
import { AdministradorDeArchivosComponent } from "./administrador-de-archivos/administrador-de-archivos.component";
import { DetallesArchivoComponent } from "./detalles-archivo/detalles-archivo.component";

export default [
    {
        path: "",
        component: FichaDocumentosComponent,
        children: [
            {
                path: ":token_carpeta",
                component: AdministradorDeArchivosComponent,
            },
            {
                path: "",
                component: AdministradorDeArchivosComponent,
            },
        ]
    }
] as Routes;