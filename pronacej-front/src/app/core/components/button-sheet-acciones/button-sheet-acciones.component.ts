import { Component, Inject, inject } from "@angular/core";
import { MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef } from "@angular/material/bottom-sheet";
import { MatListModule } from "@angular/material/list";
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from "@angular/material/icon";

export class AccionCustom {
  mostrar = true;
  declare textAccion: string;
  declare keyAccion: string;
  fontIcon = "close"
}

@Component({
  selector: 'app-acciones-usuario-component',
  templateUrl: 'button-sheet-acciones.component.scss',
  standalone: true,
  imports: [
    MatListModule,
    MatButtonModule,
    MatIconModule
  ],
})
export class AccionesUsuarioComponent {
  constructor(@Inject(MAT_BOTTOM_SHEET_DATA) public data: AccionCustom) { }

  private _bottomSheetRef =
    inject<MatBottomSheetRef<AccionesUsuarioComponent>>(MatBottomSheetRef);

  accion(accion: "editar" | "eliminar" | string) {
    this._bottomSheetRef.dismiss(accion);
  }
}