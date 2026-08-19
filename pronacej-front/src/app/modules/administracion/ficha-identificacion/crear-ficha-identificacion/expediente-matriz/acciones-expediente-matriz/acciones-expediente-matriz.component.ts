import { Component, Inject, inject } from "@angular/core";
import { MAT_BOTTOM_SHEET_DATA, MatBottomSheetRef } from "@angular/material/bottom-sheet";
import { MatListModule } from "@angular/material/list";
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from "@angular/material/icon";

export class AccionCustom {
  mostrar = true;
  declare textAccion: string;
  declare keyAccion: string
}

@Component({
  selector: 'app-acciones-expediente-matriz',
  standalone: true,
  imports: [MatListModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './acciones-expediente-matriz.component.html',
  styleUrl: './acciones-expediente-matriz.component.scss'
})
export class AccionesExpedienteMatrizComponent {
  constructor(@Inject(MAT_BOTTOM_SHEET_DATA) public data: AccionCustom) { }

  private _bottomSheetRef =
      inject<MatBottomSheetRef<AccionesExpedienteMatrizComponent>>(MatBottomSheetRef);

  accion(accion: "editar" | "eliminar" | string) {
      this._bottomSheetRef.dismiss(accion);
  }
}
