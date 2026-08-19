import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { AuditoriaSistemaVisualizarComponent } from '../auditoria-sistema-visualizar/auditoria-sistema-visualizar.component';
import { RolService } from 'app/modules/seguridad/services/rol.service';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { environment } from 'environments/environment';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { SeleccionarObjectoDelArbolComponent } from 'app/core/components/seleccionar-objecto-del-arbol/seleccionar-objecto-del-arbol.component';
import { ObjectoArbol } from 'app/core/components/seleccionar-objecto-del-arbol/ObjectoArbol.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MenuService } from 'app/modules/seguridad/services/menu.service';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';
import { PaginacionAuditoriasAccionesRequest } from 'app/core/model/request/PaginacionAuditoriasAccionesRequest.model';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-auditoria-sistema-comp',
  standalone: true,
  imports: [
    MatExpansionModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    AuditoriaSistemaVisualizarComponent,
    MatDatepickerModule,
    MatFormFieldModule,
    MatSelectModule,
    SeleccionarObjectoDelArbolComponent,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './auditoria-sistema-comp.component.html',
  styleUrl: './auditoria-sistema-comp.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'en-GB' },
    provideNativeDateAdapter()
  ]
})
export class AuditoriaSistemaCompComponent implements OnInit {

  nemonicoPantalla = etiquetasModel.NEMONICO_MENU_AUDITORIAS_SISTEMA;
  objectosArbol: ObjectoArbol<CatalogoDTO>[] = [];
  objectosArbolMenu: ObjectoArbol<MenuDTO>[] = [];

  maxDate = new Date();
  formFiltros: FormGroup;
  listRoles: RolDTO[] = [];
  listAccionesUsuario: CatalogoDTO[] = [];
  listaDeMenuDisponibles: MenuDTO[] = [];

  // funcionesUtils = new FuncionesUtils();

  @ViewChild("compSeleccionAccion") compSeleccionAccion: SeleccionarObjectoDelArbolComponent<CatalogoDTO>;
  @ViewChild("compSeleccionMenu") compSeleccionMenu: SeleccionarObjectoDelArbolComponent<CatalogoDTO>;
  @ViewChild("compAuditoriaSistemaVisualizar") compAuditoriaSistemaVisualizar: AuditoriaSistemaVisualizarComponent;

  @ViewChild("inputF1") inputF1: any;
  @ViewChild("inputF2") inputF2: any;

  filtros = new PaginacionAuditoriasAccionesRequest();

  constructor(private fb: FormBuilder,
    private rolService: RolService,
    private dialogMensajeService: DialogMensajeService,
    private catalogoService: CatalogoService,
    private menuService: MenuService,
    private funcionesUtils: FuncionesUtils,
  ) {

    this.construirForm();

  }
  ngOnInit(): void {
    this.obtenerRoles();
    this.obtenerAcciones();
    this.obtenerMenus();

  }

  construirForm() {
    this.formFiltros = this.fb.group(
      {
        fechaInicio: new FormControl({ value: null, disabled: true }, []),
        fechaFin: new FormControl({ value: null, disabled: true }, []),
        userName: [null],
        numeroDeDocumento: [null],
        tokenIdentificadorRol: [null],
        tokenIdentificadorAccion: [null],

        tokenIdentificador: [null],
        tokenIdentificadorMenu: [null]
      }
    );
  }

  obtenerAcciones() {
    this.catalogoService.obtenerHijos(etiquetasModel.ACCIONES_DEL_SISTEMA, this.nemonicoPantalla).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }

          this.listAccionesUsuario = response.data;
          this.objectosArbol = this.funcionesUtils.crearObjectoArbolConData<CatalogoDTO>(this.listAccionesUsuario,
            "tokenIdentificador", "nombre", "hijos"
          );

        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  obtenerMenus() {
    this.menuService.obtenerTodosLosMenu(this.nemonicoPantalla).subscribe(
      {
        next: (response: RespuestaPorDefecto<MenuDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaDeMenuDisponibles = response.data;
          this.objectosArbolMenu = this.funcionesUtils.crearObjectoArbolConData<MenuDTO>(this.listaDeMenuDisponibles,
            "id", "title", "children", "icon"
          );
        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  seleccionoAccion(object: CatalogoDTO) {
    if (!environment.production) {
      console.log(object);
    }
  }

  seleccionoMenu(object: MenuDTO) {
    if (!environment.production) {
      console.log(object);
    }
  }

  obtenerRoles() {
    this.rolService.obtenerRoles(this.nemonicoPantalla).subscribe(
      {
        next: (resp: RespuestaPorDefecto<RolDTO[]>) => {
          if (!environment.production) {
            console.log(resp);
          }

          if (!resp.exito) {
            this.dialogMensajeService.mensajeError(resp.mensaje);
            return;
          }

          this.listRoles = resp.data;
        },
        error: (error: any) => {
          this.rolService.checkError(error);
        }
      }
    );
  }

  consultarPorFiltros(generarReporte = false) {
    if (!environment.production) {
      console.log(this.formFiltros);
    }

    if (this.formFiltros.invalid) {
      this.formFiltros.markAllAsTouched();
      this.dialogMensajeService.mensajeError("Debes de llenar toda la información requerida para continuar");
      return;
    }

    this.filtros.fechaFin = this.getValue("fechaFin");
    this.filtros.fechaInicio = this.getValue("fechaInicio");
    this.filtros.userName = this.getValue("userName");
    this.filtros.tokenIdentificadorRol = this.getValue("tokenIdentificadorRol");
    console.log('seleccionad de menu',this.compSeleccionAccion);
    this.filtros.tokenIdentificadorAccion = this.compSeleccionAccion.objectoArbolElegido?.data?.tokenIdentificador;
    this.filtros.tokenIdentificadorMenu = this.compSeleccionMenu.objectoArbolElegido?.data?.tokenIdentificador;

    this.compAuditoriaSistemaVisualizar.consultarAcciones(generarReporte);
  }

  dateInput(keyForm: string, eventDatePicker: MatDatepickerInputEvent<Date, any>) {
    let date = eventDatePicker.value;
    if (keyForm == "fechaFin") {
      //Se sube un día a la fecha fin
      let mils = 24 * 60 * 60 * 1000;
      date.setTime(date.getTime() + mils);
    }
    this.formFiltros.get(keyForm).setValue(date.toISOString());
  }

  private getValue(key: string) {
    return this.formFiltros.get(key)?.value;
  }

  borrarFiltros() {
    this.formFiltros.reset();
    this.compSeleccionMenu.objectoArbolElegido = null;
    this.compSeleccionAccion.objectoArbolElegido = null;

    this.inputF1.nativeElement.value = null;
    this.inputF2.nativeElement.value = null;
  }
}
