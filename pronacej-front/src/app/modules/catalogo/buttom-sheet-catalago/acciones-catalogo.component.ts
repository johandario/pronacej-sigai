import { Component, Inject, inject } from "@angular/core";
import { MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef } from "@angular/material/bottom-sheet";
import { MatListModule } from "@angular/material/list";
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from "@angular/material/icon";

export class AccionesCatalogo {
    mostrar = true;
    subCatalogo: boolean;
    declare textAccion: string;
    declare keyAccion: string
}

@Component({
    selector: 'app-acciones-catalogo-component',
    templateUrl: 'acciones-catalogo.component.html',
    standalone: true,
    imports: [MatListModule,
        MatButtonModule,
        MatIconModule
    ],
})
export class AccionesCatalogoComponent {
    constructor(@Inject(MAT_BOTTOM_SHEET_DATA) public data: AccionesCatalogo) { }

    private readonly _bottomSheetRef =
        inject<MatBottomSheetRef<AccionesCatalogoComponent>>(MatBottomSheetRef);

    accion(accion: "editar" | "eliminar" | string) {
        this._bottomSheetRef.dismiss(accion);
    }
}