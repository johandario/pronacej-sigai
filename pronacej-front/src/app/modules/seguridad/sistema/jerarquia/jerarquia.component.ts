import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from '../../services/jerarquia.service';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import etiquetasModel from 'app/core/etiquetas.model';
import { MatDialog } from '@angular/material/dialog';
import { JerarquiaDialogComponent } from './jerarquia-dialog/jerarquia-dialog.component';
import { ConfirmDialogComponent } from '../util/confirm-dialog/confirm-dialog.component';
import { WarningDialogComponent } from '../util/warning-dialog/warning-dialog.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';

@Component({
  selector: 'app-jerarquia',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatExpansionModule,
    MatAccordion,
    MatIcon
  ],
  templateUrl: './jerarquia.component.html',
  styleUrl: './jerarquia.component.scss'
})
export class JerarquiaComponent implements OnInit{
  jerarquia: JerarquiaDTO[] = [];
  newNodeId: number = 9; // Un contador para los nuevos nodos
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_JERARQUIA;

  constructor(private jerarquiaService: JerarquiaService, private dialog: MatDialog, private dialogMensajeService: DialogMensajeService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
      this.jerarquia = data.data || [];
      this.cdr.detectChanges();
      // Verificar nodos raíz
      const nodosRaiz = this.jerarquia.filter(nodo => nodo.idJerarquiaPadre === null);
    });
  }

  getChildren(idPadre: number): JerarquiaDTO[] {
    return this.jerarquia.filter(j => j.idJerarquiaPadre === idPadre);
  }

  hasChildren(idPadre: number): boolean {
    return this.jerarquia.some(j => j.idJerarquiaPadre === idPadre);
  }

  update(): void {
    this.jerarquiaService.obtenerJerarquias(this.nemonicoMenu).subscribe(data => {
      this.jerarquia = data.data;
    });
  }

  addNew(): void {
    const dialogRef = this.dialog.open(JerarquiaDialogComponent, {
      width: '500px',
      data: { type: 0, list: this.jerarquia },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

        // Comprobar si el nombre ya existe en la jerarquía
        const nombreDuplicado = this.jerarquia.some(nodo => nodo.nombre.toLowerCase() === result.nombre.toLowerCase());

        if (nombreDuplicado) {
          this.openWarningDialog('El nombre ingresado ya existe en la jerarquía. Por favor, elige otro nombre.');
          return;
        }

        // Guardar en el servicio la nueva jerarquía
        const nuevoHijo: JerarquiaDTO = {
          id: 0,
          idJerarquiaPadre: result.idJerarquiaPadre, // El padre es el nodo actual
          nombre: result.nombre,
          empresa: result.empresa,
          direccion: result.direccion,
          tokenIdentificadorGenero: result.genero,          
          jerarquiaPadre: null
        };

        this.jerarquiaService.crearJerarquia(nuevoHijo, this.nemonicoMenu).subscribe(data => {
          // ✅ USAR EL MENSAJE DEL BACKEND
          let ref = this.dialogMensajeService.mensajeExitoso(
            `${'Guardar'}`,
            data.mensaje);  // 📩 Mensaje que viene del backend
          this.update();
        });
      }
    });
  }

  addChild(nodeId: number): void {
    const dialogRef = this.dialog.open(JerarquiaDialogComponent, {
      width: '500px',
      data: { type: 1, list: this.jerarquia, id: nodeId },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

        // Comprobar si el nombre ya existe en la jerarquía
        const nombreDuplicado = this.jerarquia.some(nodo => nodo.nombre.toLowerCase() === result.nombre.toLowerCase());

        if (nombreDuplicado) {
          this.openWarningDialog('El nombre ingresado ya existe en la jerarquía. Por favor, elige otro nombre.');
          return;
        }

        // Guardar en el servicio la nueva jerarquía
        const nuevoHijo: JerarquiaDTO = {
          id: 0,
          idJerarquiaPadre: result.idJerarquiaPadre, // El padre es el nodo actual
          nombre: result.nombre,
          empresa: result.empresa,
          jerarquiaPadre: null,
          direccion: result.direccion,
          tokenIdentificadorGenero: result.genero
        };

        this.jerarquiaService.crearJerarquia(nuevoHijo, this.nemonicoMenu).subscribe(data => {
          // ✅ USAR EL MENSAJE DEL BACKEND
          let ref = this.dialogMensajeService.mensajeExitoso(
            `${'Guardar'}`,
            data.mensaje);  // 📩 Mensaje que viene del backend

          // Actualizar la jerarquía local después de guardar en el servicio
          this.jerarquia.push(nuevoHijo);
        });
      }
    });
  }

  editItem(nodeId: number): void {
    const dialogRef = this.dialog.open(JerarquiaDialogComponent, {
      width: '500px',
      data: { type: 2, list: this.jerarquia, id: nodeId },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Guardar en el servicio la nueva jerarquía
        const nuevoHijo: JerarquiaDTO = {
          id: nodeId,
          idJerarquiaPadre: result.idJerarquiaPadre,
          nombre: result.nombre,
          empresa: result.empresa,
          jerarquiaPadre: null,
          direccion: result.direccion,
          tokenIdentificadorGenero: result.genero
        };

        console.log(nuevoHijo);
        this.jerarquiaService.actualizarJerarquia(nuevoHijo, this.nemonicoMenu).subscribe(data => {
          // ✅ USAR EL MENSAJE DEL BACKEND
          let ref = this.dialogMensajeService.mensajeExitoso(
            `${'Editar'}`,
            data.mensaje);  // 📩 Mensaje que viene del backend

          // Actualizar la jerarquía local después de guardar en el servicio
          this.update();
        });
      }
    });
  }

  deleteItem(nodeId: number): void {
    if (this.hasChildren(nodeId)) {
      // Si el nodo tiene hijos, mostramos una alerta o diálogo informativo
      this.openWarningDialog('No se puede eliminar este nodo porque tiene hijos.');
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '500px',
      data: { nodeId },
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Si se confirma la eliminación, proceder a eliminar
        let jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO = this.jerarquia.find(nodo => nodo.id == nodeId);

        this.jerarquiaService.removerJerarquia(jerarquiaDTO, this.nemonicoMenu).subscribe(data => {
          // ✅ USAR EL MENSAJE DEL BACKEND
          let ref = this.dialogMensajeService.mensajeExitoso(
            `${'Eliminar'}`,
            data.mensaje);  // 📩 Mensaje que viene del backend

          this.update();
        });
      }
    });
  }

  openWarningDialog(message: string): void {
    this.dialog.open(WarningDialogComponent, {
      width: '500px',
      data: { message },
      disableClose: true
    });
  }

}