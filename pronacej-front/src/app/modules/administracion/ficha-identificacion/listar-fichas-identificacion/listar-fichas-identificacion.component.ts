import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { Component } from '@angular/core';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { AccionesUsuarioComponent } from 'app/core/components/button-sheet-acciones/button-sheet-acciones.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { environment } from 'environments/environment.development';
import { FichaIdentificacionService } from '../../services/fichaIdentificacion.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { PaginacionRequestFichaIdentificacion } from 'app/core/model/request/PaginacionRequestFichaIdentificacion.model';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import * as XLSX from 'xlsx';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-listar-fichas-identificacion',
  standalone: true,
  imports: [
    MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    CommonModule,
    MatSelectModule,
    MatSortModule,
    MatSlideToggleModule
  ],
  templateUrl: './listar-fichas-identificacion.component.html',
  styleUrl: './listar-fichas-identificacion.component.scss'
})
export class ListarFichasIdentificacionComponent {

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "Expedientes matriz";

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_LISTAR_FICHAS;

  listaFichasIdentificacion: FichaIdentificacionDTO[] = [];
  listaFichasIdentificacionTodas: FichaIdentificacionDTO[] = [];
  dataSource: CdkTableDataSourceInput<FichaIdentificacionDTO>;

  keyLabelsTable: any = {
    numero: "No.",
    acciones: "Acciones",
    nombreTipoDocumento: "Tipo documento",
    numeroDocumento: "N° documento",
    fechaNacimiento: "Fecha nacimiento",
    nombres: "Nombres",
    apellidos: "Apellidos",
    nombreSexo: "Sexo",
    nacionalidad: "Nacionalidad",
    ocupacion: "Ocupación",
    tipoEstadoCivil: "Estado civil",
    impedimentoDiscapacidad: "Impedimento discapacidad",
  };

  filter: string = '';
  centros: JerarquiaDTO[] = [];
  
  // Inicializar centro con un objeto por defecto para evitar errores de undefined
  centro: JerarquiaDTO = {
    tokenIdentificador: '',
    nombre: '',
    nemonico: '',
    esOficinaCentral: false,
    jerarquiaPadre: {
      tokenIdentificador: '',
      nombre: '',
      nemonico: '',
      esOficinaCentral: false
    }
  } as JerarquiaDTO;
  
  filtroCentro: string = '';

  ingresosPaginacion: PaginacionRequest = new PaginacionRequest();
  todosEstados: boolean = false;
  postEgreso: boolean = false;

  constructor(
    private authSerguridadServicio: AuthSerguridadServicio,
    private fichaIdentificacionService: FichaIdentificacionService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private jerarquiaService: JerarquiaService,
    private funcionesUtils: FuncionesUtils,
  ) { }

  ngOnInit(): void {
    this.obtenerFichasIdentificacion();
    this.cargarCentro();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  activarAcciones(fichaIdentificacionDTO: FichaIdentificacionDTO) {
    let ref = this.accionesSheet.open(AccionesUsuarioComponent,
      {
        data: {
          mostrar: false,
          textAccion: "",
          keyAccion: "",
        }
      }
    );

    ref.afterDismissed().subscribe(
      {
        next: (result: "editar" | "eliminar" | "Desbloquear" | "Bloquear") => {
          if (result == "editar") {
            this.editarFichaIdentificacion(fichaIdentificacionDTO);
          } else if (result == "eliminar") {
            this.eliminarFichaIdentificacion(fichaIdentificacionDTO);
          }
        }
      }
    );
  }

  editarFichaIdentificacion(fichaIdentificacionDTO: FichaIdentificacionDTO) {
    this.router.navigate(['crear-editar/fichaPrincipal/' + fichaIdentificacionDTO.tokenIdentificador], { state: { fichaIdentificacionDTO }, relativeTo: this.route });
  }

  eliminarFichaIdentificacion(fichaIdentificacionDTO: FichaIdentificacionDTO) {
    let ref = this.dialogMensajeService.mensajeConConfirmacion(
      "Estás seguro de eliminar la ficha con número de documento: \"" + fichaIdentificacionDTO.numeroDocumento + "\" esta operación es irreversible",
      "Deseas continuar?"
    );

    ref.afterClosed().subscribe(
      {
        next: (resp: "confirmed" | "cancelled") => {
          if (resp == "confirmed") {
            let load = this.dialogMensajeService.mensajeLoading("Eliminando la ficha de identificación..");
            this.fichaIdentificacionService.eliminarFichaIdentificacion(fichaIdentificacionDTO, etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION).subscribe(
              {
                next: (resp: RespuestaPorDefecto<boolean>) => {
                  load.close();
                  this.dialogMensajeService.mensajeExitoso(resp.titulo, resp.mensaje);

                  if (!resp.exito) {
                    return;
                  }

                  this.obtenerFichasIdentificacion();
                },
                error: (error: any) => {
                  load.close();
                  this.fichaIdentificacionService.checkError(error);
                }
              }
            );
          }
        }
      }
    );
  }

  obtenerFichasIdentificacion() {
    let paginacionRequest = new PaginacionRequestFichaIdentificacion();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.sort = this.ingresosPaginacion.sort;
    paginacionRequest.direction = this.ingresosPaginacion.direction;
    console.log('todos estados', this.todosEstados);
    paginacionRequest.todosEstados = this.todosEstados;
    paginacionRequest.postEgreso = this.postEgreso;

    if (this.filter.length > 0) {
      paginacionRequest.filter = this.filter;
      this.page = 0;
    }
    if (this.filtroCentro.length > 0 && this.filtroCentro != '0') {
      paginacionRequest.tokenCentro = this.filtroCentro;
    }

    this.fichaIdentificacionService.obtenerFichasIdentificacionPaginado(paginacionRequest, etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaIdentificacionDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaFichasIdentificacion = response.data.data || [];
          this.dataSource = this.listaFichasIdentificacion;
          this.totalItems = response.data.totalItems || 0;
          // this.obtenerFichasIdentificacionTodas();
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  descargarFichasIdentificacionTodas() {
    
    let paginacionRequest = new PaginacionRequestFichaIdentificacion();
    paginacionRequest.size = this.totalItems;
    paginacionRequest.page = 0;
    paginacionRequest.todosEstados = this.todosEstados;
    paginacionRequest.postEgreso = this.postEgreso;   

    if (this.totalItems < 1) {
      this.dialogMensajeService.mensajeAdvertencia("Atención", "La lista se encuentra vacía. No es posible exportar dado que no existen elementos disponibles.");
      return;
    }
    this.fichaIdentificacionService.obtenerFichasIdentificacionPaginado(paginacionRequest, etiquetasModel.NEMONICO_MENU_FICHA_IDENTIFICACION).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaIdentificacionDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaFichasIdentificacionTodas = response.data.data || [];     
          this.onXLSX();     
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }

  agregarFichaIdentificacion() {
    this.router.navigate(['crear-editar/fichaPrincipal'], { relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerFichasIdentificacion();
  }

  cargarCentros(): void {
    this.jerarquiaService
      .obtenerJerarquiasPorJerarquiaPadreFuncionario(this.nemonicoMenu)
      .subscribe({
        next: (resp: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (resp.exito) {
            this.centros = resp.data || [];
            console.log('Centros cargados:', this.centros);
          } else {
            console.warn('Ocurrió un problema al cargar los centros:', resp.mensaje);
          }
        },
        error: (error: any) => {
          console.error('Error al cargar los centros:', error);
        }
      });
  }

  cargarCentro() {
    this.jerarquiaService
      .obtenerJerarquiaPorNumeroDeDocumento(this.nemonicoMenu)
      .subscribe({
        next: (respuesta: RespuestaPorDefecto<JerarquiaDTO>) => {
          if (!environment.production) {
            console.log(respuesta.data);
          }
          if (!respuesta.exito) {
            this.jerarquiaService.checkError(respuesta);
            return;
          }

          // Asignar los datos recibidos al centro inicializado
          this.centro = respuesta.data || this.centro;
          console.log('centro', this.centro);
          
          // Verificar si existe el centro y sus propiedades antes de acceder
          if (this.centro?.esOficinaCentral && this.centro?.jerarquiaPadre) {
            this.cargarCentros();
            // if (this.centro.jerarquiaPadre.nemonico == 'SOA' || this.centro.jerarquiaPadre.nemonico == 'CJDR') {
            // }
          }
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
  }

  onChangeCentro(nuevoCentroId: string): void {
    this.filtroCentro = nuevoCentroId;
  }

  onToggleChange(event: any) {
    console.log("Valor del toggle:", event.checked);
    // Ejecutar el método según el valor del toggle
    this.obtenerFichasIdentificacion();
  }

  verFicha(fichaIdentificacionDTO: FichaIdentificacionDTO) {
    fichaIdentificacionDTO.esVisualizacion = true;
    console.log('ficha saliendo', fichaIdentificacionDTO);
    this.router.navigate(['crear-editar/fichaPrincipal/' + fichaIdentificacionDTO.tokenIdentificador], { state: { fichaIdentificacionDTO }, relativeTo: this.route });
  }

  onXLSX() {
    // Transformamos los datos excluyendo "acciones"
    const transformedData = this.listaFichasIdentificacionTodas.map((item, i) => {
      const transformedItem: any = {};

      Object.keys(this.keyLabelsTable).forEach(key => {
        if (key !== 'acciones' && key !== 'nacionalidad') { // Excluir 'acciones'
          const label = this.keyLabelsTable[key] || key;

          // Manejo de campos especiales
          if (key === 'numero') {
            transformedItem[label] = this.totalItems - (i + (this.page * this.size));
          } else if (key.includes('fecha')) {
            transformedItem[label] = this.funcionesUtils.getOnlyDate(item[key]);
          } else if (key.includes('apellidos')) {
            transformedItem[label] = item['apellidoPaterno'] + ' ' + item['apellidoMaterno'];
          } else if (key.includes('impedimentoDiscapacidad')) {
            transformedItem[label] = item['impedimentoDiscapacidad'] ? 'Sí' : 'No';
          } else {
            transformedItem[label] = item[key];
          }
        }
      });

      return transformedItem;
    });

    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet([]);

    const headers = Object.values(this.keyLabelsTable).filter((label) => label !== 'Acciones' && label !== 'Nacionalidad');

    const title = ["Reporte de Fichas de Identificación"];

    XLSX.utils.sheet_add_aoa(ws, [title], { origin: "A1" });

    // Fusionar celdas del título para abarcar todas las columnas
    ws["!merges"] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: headers.length - 1 } }];

    // Agregar encabezados en la segunda fila (A2)
    XLSX.utils.sheet_add_aoa(ws, [headers], { origin: "A2" });

    // Agregar los datos a partir de la tercera fila (A3)
    XLSX.utils.sheet_add_json(ws, transformedData, { origin: "A3", skipHeader: true });

    ws['!cols'] = headers.map(() => ({ wch: 20 }));

    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Datos');

    XLSX.writeFile(wb, 'datos.xlsx');
  }

  getLocalDate(date: Date) {
    return this.funcionesUtils.getLocalDate(date);
  }

  getOnlyDate(date: Date) {
    return this.funcionesUtils.getOnlyDate(date);
  }

  onSortChange(evento: Sort) {
    if (evento.direction) {
      this.ingresosPaginacion.sort = evento.active;
      this.ingresosPaginacion.direction = evento.direction;
    } else {
      this.ingresosPaginacion.sort = null;
      this.ingresosPaginacion.direction = null;
    }
    this.obtenerFichasIdentificacion();
  }

  capitalizeFirstLetter(texto: string): string {
    if (!texto) return ''; 
    return texto.charAt(0).toUpperCase() + texto.slice(1).toLowerCase();
  }
}