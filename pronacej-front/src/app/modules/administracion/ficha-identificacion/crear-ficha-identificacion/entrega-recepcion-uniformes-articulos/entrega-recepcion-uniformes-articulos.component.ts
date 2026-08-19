import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { VisualizarRecepcionEntregaComponent } from './visualizar/visualizar-recepcion-entrega.component';
import { CrearEditarRecepcionEntregaComponent } from './crear-editar/crear-editar-recepcion-entrega.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';

@Component({
  selector: 'app-entrega-recepcion-uniformes-articulos',
  standalone: true,
  imports: [  
    MatButtonModule,
    MatIconModule,
  ],  
  templateUrl: './entrega-recepcion-uniformes-articulos.component.html',
  styleUrl: './entrega-recepcion-uniformes-articulos.component.scss'
})
export class EntregaRecepcionUniformesArticulosComponent {
  estadoCrear: boolean = false;
  estadoEditar: boolean = false;

  keyLabelsTable: any = {    
    acciones: "",
    numRegistro: "Número de registro",
    tipoRegistro: "Tipo",
    fechaRegistro: "Fecha"
  };  

  constructor(    
    private dialogMensajeService: DialogMensajeService,
  ) { }

  guardarRegistro() {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      `${this.estadoEditar ? 'Se actualizarán los datos ingresados al registro existente.' : 'Se guardará el nuevo registro con la información ingresada.'}`,
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            this.estadoCrear = false;
          }
        }
      }
    );
  }

  recibirValor(valor: boolean) {
    this.estadoCrear = valor;
    this.estadoEditar = true;
  }

}
