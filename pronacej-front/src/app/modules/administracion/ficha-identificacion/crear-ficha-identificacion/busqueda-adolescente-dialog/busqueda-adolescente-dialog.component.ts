import { Component, inject, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Router } from '@angular/router';
import { TablaDatosComponent } from 'app/core/components/tabla-datos/tabla-datos.component';
import { FichaIdentificacionResumenDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { environment } from 'environments/environment';

@Component({
  selector: 'app-busqueda-adolescente-dialog',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButtonModule,
    TablaDatosComponent
  ],
  templateUrl: './busqueda-adolescente-dialog.component.html',
  styleUrl: './busqueda-adolescente-dialog.component.scss'
})
export class BusquedaAdolescenteDialogComponent implements OnInit {
  private fichaIdentificacionService = inject(FichaIdentificacionService);
  private dialogMensajeService = inject(DialogMensajeService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  listaAdolescentes: FichaIdentificacionResumenDTO[] = [];
  etiquetasColumnas: any = {
    acciones: "Acciones",
    nombreCompleto: "Nombre Completo",
    numeroIdentificacion: "Número de Identificación",
    centro: "Centro",
    estado: "Estado"
  };

  paginacion: Paginacion = new Paginacion();
  terminoBusqueda: string = '';

  constructor() { }

  ngOnInit(): void {
    this.obtenerAdolescentes();
  }

  obtenerAdolescentes() {
    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.size = this.paginacion?.pageSize || 5;
    paginacionRequest.page = this.paginacion?.pageIndex ?? 0;
    paginacionRequest.filter = this.terminoBusqueda || '';

    this.fichaIdentificacionService.obtenerFichasIdentificacionResumido(paginacionRequest).subscribe({
      next: (response) => {
        if (!environment.production) {
          console.log('Respuesta obtenerPermisos:', response);
        }

        if (!response.exito) {
          this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
          return;
        }

        // Procesar los datos recibidos
        let datos = response.data.data;
        this.listaAdolescentes = datos;
        this.paginacion.totalItems = response.data.totalItems;
      },
      error: (error: any) => {
        console.error('Error al obtener roles:', error);
      }
    });
  } 

  onBuscar(termino: string) {
    this.terminoBusqueda = termino;
    this.paginacion.pageIndex = 0; // Volver a la primera página
    this.obtenerAdolescentes();
  }

  onCambiarPagina(evento: PageEvent) {
    this.paginacion.pageSize = evento.pageSize || 5;
    this.paginacion.pageIndex = evento.pageIndex || 0;
    this.obtenerAdolescentes();
  }  

  seleccionarAdolescente(adolescente: FichaIdentificacionResumenDTO) {
    const confirmacionDialog = this.dialogMensajeService.mensajeConConfirmacion(
      'Confirmar selección', 
      `¿Está seguro que desea obtener los datos del adolescente ${adolescente.nombreCompleto}?`
    );

    confirmacionDialog.afterClosed().subscribe((resp: "confirmed" | "cancelled") => {
      if (resp === "confirmed") {
        let uuid = adolescente.tokenIdentificador || '';

        this.router.navigateByUrl(
          `/gestion-adolescente/ficha-identificacion/crear-editar/fichaPrincipal/${uuid}`
        ).then(() => {
          window.location.reload();
        });
      }
    });    
  }
}
