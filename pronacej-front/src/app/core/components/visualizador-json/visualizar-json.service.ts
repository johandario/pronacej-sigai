import { Injectable } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { VisualizarJsonModalComponent } from "./visualizar-json-modal/visualizar-json-modal.component";

@Injectable(
    {
        providedIn: "root"
    }
)
export class VisualizarJsonService {

    constructor(private matdialog: MatDialog
    ) { }

    abrirVistaDeJson(titulo: string, jsonString: string) {
        let ref = this.matdialog.open(
            VisualizarJsonModalComponent,
            {
                data: {
                    titulo: titulo,
                    jsonString: jsonString
                },
                hasBackdrop: false
            }
        );

        return ref;
    }
}