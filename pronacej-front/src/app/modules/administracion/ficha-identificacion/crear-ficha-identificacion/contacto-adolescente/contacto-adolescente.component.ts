import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component } from '@angular/core';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { ContactoAdolescenteDTO } from 'app/core/model/both/ia/contactoAdolescenteDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { ContactoAdolescenteService } from 'app/modules/administracion/services/contactoAdolescente.service';
import { environment } from 'environments/environment';

@Component({
 selector: 'app-contacto-adolescente',
 standalone: true,
 imports: [
   MatTableModule,
   MatBottomSheetModule,
   MatButtonModule,
   MatPaginatorModule,
   MatIconModule,
   MatCardModule,
   MatInputModule,
 ],
 templateUrl: './contacto-adolescente.component.html',
 styleUrl: './contacto-adolescente.component.scss'
})
export class ContactoAdolescenteComponent {
 
 uuid_fp: string;
 pagina = 0;
 tamanoLista = [5, 10, 15, 20];
 tamano = this.tamanoLista[0];
 totalElementos = 0;

 tituloPantalla: string = "contacto con adolescente";

 listaContactos: ContactoAdolescenteDTO[] = [];
 fuenteDatos: CdkTableDataSourceInput<ContactoAdolescenteDTO>;

 etiquetasTabla: any = {
   numero: "No.",
   acciones: "Acciones",
   modalidadEntrevista: "Modalidad de entrevista",
   descripcionActividad: "Descripción de actividad",
   fechaRegistro: "Fecha registro",
   usuarioRegistro: "Usuario que registró"
 };

 constructor(
   private servicioContactoAdolescente: ContactoAdolescenteService,
   private servicioDialogoMensaje: DialogMensajeService,
   private enrutador: Router,
   private ruta: ActivatedRoute,
   public funcionesUtils: FuncionesUtils,
 ) { }

 ngOnInit(): void {
   this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
   this.obtenerListadoContactos();
 }

 obtenerClaves() {
   return Object.keys(this.etiquetasTabla);
 }

 visualizarContacto(contactoDTO: ContactoAdolescenteDTO) {
   contactoDTO.esVisualizacion = true;
   this.enrutador.navigate(['crear-editar'], {
     state: { contactoDTO },
     relativeTo: this.ruta
   });
 }

 editarContacto(contactoDTO: ContactoAdolescenteDTO) {
   this.enrutador.navigate(['crear-editar'], {
     state: { contactoDTO },
     relativeTo: this.ruta
   });
 }

 eliminarContacto(contactoDTO: ContactoAdolescenteDTO) {
   let referencia = this.servicioDialogoMensaje.mensajeConConfirmacion(
     "¿Estás seguro de eliminar el contacto? Esta operación es irreversible",
     "¿Deseas continuar?"
   );

   referencia.afterClosed().subscribe({
     next: (respuesta: "confirmed" | "cancelled") => {
       if (respuesta == "confirmed") {
         let carga = this.servicioDialogoMensaje.mensajeLoading("Eliminando contacto...");
         this.servicioContactoAdolescente.eliminarContacto(contactoDTO).subscribe({
           next: (respuesta: RespuestaPorDefecto<boolean>) => {
             carga.close();
             this.servicioDialogoMensaje.mensajeExitoso(respuesta.titulo, respuesta.mensaje);

             if (!respuesta.exito) {
               return;
             }

             this.obtenerListadoContactos();
           },
           error: (error: any) => {
             carga.close();
             this.servicioContactoAdolescente.verificarError(error);
           }
         });
       }
     }
   });
 }

 agregarContacto() {
   this.enrutador.navigate(['crear-editar'], { relativeTo: this.ruta });
 }

 obtenerListadoContactos() {
   let solicitudPaginacion = new PaginacionRequest();
   solicitudPaginacion.size = this.tamano;
   solicitudPaginacion.page = this.pagina;
   solicitudPaginacion.tokenIdentificador = this.uuid_fp;

   this.servicioContactoAdolescente.obtenerContactosPaginado(
     solicitudPaginacion,
     etiquetasModel.NEMONICO_MENU_CONTACTO_ADOLESCENTE
   ).subscribe({
     next: (respuesta: RespuestaPorDefecto<PaginacionResponse<ContactoAdolescenteDTO>>) => {
       if (!environment.production) {
         console.log(respuesta);
       }

       if (!respuesta.exito) {
         this.servicioDialogoMensaje.mensajeErrorConTitulo(respuesta.titulo, respuesta.mensaje);
         return;
       }

       this.listaContactos = respuesta.data.data;
       this.fuenteDatos = this.listaContactos;
       this.totalElementos = respuesta.data.totalItems;
     },
     error: (error: any) => {
       console.log(error);
     }
   });
 }

 manejarEventoPaginacion(eventoPaginacion: PageEvent) {
   this.tamano = eventoPaginacion.pageSize;
   this.pagina = eventoPaginacion.pageIndex;
   this.obtenerListadoContactos();
 }
}