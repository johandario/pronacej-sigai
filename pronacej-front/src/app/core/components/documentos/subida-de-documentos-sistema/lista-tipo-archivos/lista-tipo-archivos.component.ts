import { Component, Input } from '@angular/core';
import { TipoDeDocumento } from '../../modelos/TipoDeDocumento.model';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-lista-tipo-archivos',
  standalone: true,
  imports: [
    MatTooltipModule,
    MatIconModule
  ],
  templateUrl: './lista-tipo-archivos.component.html',
  styleUrl: './lista-tipo-archivos.component.scss'
})
export class ListaTipoArchivosComponent {

  @Input({ required: true }) tiposDeDocumentosSistema: TipoDeDocumento[];
  @Input({ required: true }) declare titulo: string;

}
