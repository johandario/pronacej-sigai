import { Routes } from "@angular/router";
import { HistoriaClinicaComponent } from "./historia-clinica.component";

export default [
    {
        path: "",
        children: [
            {
                path: "",
                component: HistoriaClinicaComponent,
            },
               
        ]
    },
] as Routes;