import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { ContactoAdolescenteDTO } from 'app/core/model/both/ia/contactoAdolescenteDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { ContactoAdolescenteService } from 'app/modules/administracion/services/contactoAdolescente.service';

@Component({
 selector: 'app-contacto-adolescente-crear-editar',
 standalone: true,
 imports: [
   CommonModule,
   MatExpansionModule,
   ReactiveFormsModule,
   MatInputModule,
   MatButtonModule,
   MatSelectModule,
   MatFormFieldModule,
   MatIconModule,
   MatDatepickerModule,
 ],
 templateUrl: './contacto-adolescente-crear-editar.component.html'
})
export class ContactoAdolescenteCrearEditarComponent {

 uuid_fp: string;
 contactoForm: FormGroup;
 contactoDTO: ContactoAdolescenteDTO;
 tituloPantalla = "contacto con adolescente";

 esEdicion = false;
 esVisualizacion = false;
 nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_CONTACTO_ADOLESCENTE;

 listaModalidadEntrevista: CatalogoDTO[] = [];
 listaEtapa: CatalogoDTO[] = [];
 listaTipoContacto: CatalogoDTO[] = [];

 constructor(
   private formBuilder: FormBuilder,
   private servicioDialogoMensaje: DialogMensajeService,
   private servicioContactoAdolescente: ContactoAdolescenteService,
   private enrutador: Router,
   private ruta: ActivatedRoute,
   public funcionesUtils: FuncionesUtils,
 ) {
   this.construirForm();
 }

 ngOnInit(): void {
   this.uuid_fp = this.ruta.snapshot.params['uuid_fp'];
   this.contactoDTO = history.state.contactoDTO;
   this.cargarCatalogos();

   if (this.contactoDTO) {
     this.esVisualizacion = this.contactoDTO.esVisualizacion;

     if (this.esVisualizacion) {
       this.contactoForm.disable();
     }
     this.empezarEdicion(this.contactoDTO);
   }
 }

 construirForm() {
   this.contactoForm = this.formBuilder.group({
     tokenIdentificadorModalidadEntrevista: ['0', [Validators.required]],
     tokenIdentificadorEtapa: ['0', [Validators.required]],
     tokenIdentificadorTipoContacto: ['0', [Validators.required]],
     fechaHora: [null, [Validators.required]],
     descripcionActividad: [null, [Validators.required]],
     observacionesSugerencias: [null, [Validators.required]]
   });
 }

 cargarCatalogos() {
   this.funcionesUtils.obtenerListaCatalogo('MODALIDAD_ENTREVISTA', this.nemonicoMenu).subscribe({
     next: (datos) => this.listaModalidadEntrevista = datos,
     error: (error) => console.error('Error cargando modalidades:', error)
   });

   this.funcionesUtils.obtenerListaCatalogo('ETAPA', this.nemonicoMenu).subscribe({
     next: (datos) => this.listaEtapa = datos,
     error: (error) => console.error('Error cargando etapas:', error)
   });

   this.funcionesUtils.obtenerListaCatalogo('TIPO_CONTACTO', this.nemonicoMenu).subscribe({
     next: (datos) => this.listaTipoContacto = datos,
     error: (error) => console.error('Error cargando tipos de contacto:', error)
   });
 }

 empezarEdicion(contactoEditar: ContactoAdolescenteDTO) {
   this.esEdicion = true;
   this.contactoDTO = contactoEditar;

   this.contactoForm.patchValue({
     tokenIdentificadorModalidadEntrevista: contactoEditar.tokenIdentificadorModalidadEntrevista,
     tokenIdentificadorEtapa: contactoEditar.tokenIdentificadorEtapa,
     tokenIdentificadorTipoContacto: contactoEditar.tokenIdentificadorTipoContacto,
     fechaHora: contactoEditar.fechaHora,
     descripcionActividad: contactoEditar.descripcionActividad,
     observacionesSugerencias: contactoEditar.observacionesSugerencias
   });
 }

 private obtenerValor(llave: string) {
   return this.contactoForm.get(llave)?.value;
 }

 crearActualizar() {
   this.contactoForm.disable();

   let contacto = new ContactoAdolescenteDTO();
   contacto.tokenIdentificadorModalidadEntrevista = this.obtenerValor("tokenIdentificadorModalidadEntrevista");
   contacto.tokenIdentificadorEtapa = this.obtenerValor("tokenIdentificadorEtapa");
   contacto.tokenIdentificadorTipoContacto = this.obtenerValor("tokenIdentificadorTipoContacto");
   contacto.fechaHora = this.obtenerValor("fechaHora");
   contacto.descripcionActividad = this.obtenerValor("descripcionActividad");
   contacto.observacionesSugerencias = this.obtenerValor("observacionesSugerencias");

   contacto.tokenIdentificadorFichaIdentificacion = this.uuid_fp;
   contacto.tokenIdentificador = this.contactoDTO?.tokenIdentificador;
   contacto.esEdicion = this.esEdicion;

   this.servicioContactoAdolescente.crearContacto(contacto, this.nemonicoMenu).subscribe({
     next: (respuesta: RespuestaPorDefecto<ContactoAdolescenteDTO>) => {
       this.contactoForm.enable();

       if (!respuesta.exito) {
         this.servicioContactoAdolescente.verificarError(respuesta);
         return;
       }
       this.servicioDialogoMensaje.mensajeExitoso(respuesta.titulo, respuesta.mensaje);
       this.enrutador.navigate(['../'], { relativeTo: this.ruta });
     },
     error: (error: any) => {
       this.servicioContactoAdolescente.verificarError(error);
       this.contactoForm.enable();
     }
   });
 }

 cancelarEdicion() {
   this.esEdicion = false;
   this.contactoForm.reset();
   this.contactoDTO = null;
   this.enrutador.navigate(['../'], { relativeTo: this.ruta });
 }
}