import { Component } from '@angular/core';
import { EnvioNotificacionComponent } from 'app/core/components/notificacion/envio-notificacion/envio-notificacion.component';
import etiquetasModel from 'app/core/etiquetas.model';

@Component({
  selector: 'app-notificar-documentacion',
  standalone: true,
  imports: [
    EnvioNotificacionComponent
  ],
  templateUrl: './notificar-documentacion.component.html',
  styleUrl: './notificar-documentacion.component.scss'
})
export class NotificarDocumentacionComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_NOTIFICACIONES_DOCUMENTACION;

}
