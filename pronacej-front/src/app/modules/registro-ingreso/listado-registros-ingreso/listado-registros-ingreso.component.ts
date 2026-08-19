import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatBottomSheet, MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Router, ActivatedRoute } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionRequestFichaIdentificacion } from 'app/core/model/request/PaginacionRequestFichaIdentificacion.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { AuthSerguridadServicio } from 'app/modules/seguridad/services/auth.seguridad.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { environment } from 'environments/environment';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-listado-registros-ingreso',
  standalone: true,
  imports: [MatTableModule,
    MatBottomSheetModule,
    MatButtonModule,
    MatPaginatorModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatSelectModule,
    CommonModule,
    MatSortModule],
  templateUrl: './listado-registros-ingreso.component.html',
  styleUrl: './listado-registros-ingreso.component.scss'
})
export class ListadoRegistrosIngresoComponent implements OnInit {

  page = 0;
  listSize = [5, 10, 15, 20];
  size = this.listSize[0];
  totalItems = 0;

  tituloPantalla: string = "Registro de Ingreso al Centro: ";
  nombreCentro: string = "";
  centro: JerarquiaDTO;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_LISTAR_FICHAS;
  listaFichasIdentificacion: FichaIdentificacionDTO[] = [];
  dataSource: CdkTableDataSourceInput<FichaIdentificacionDTO>;

  keyLabelsTable: any = {
    acciones: "Acciones",
    nombres: "Nombres",
    apellidos: "Apellidos",
    nombreSexo: "Sexo",
    fechaIngreso: "Fecha de ingreso",
    fechaNacimiento: "Fecha de nacimiento",
    juzgado: "Organo jurisdiccional",
    juez: "Funcionario",
    observacionIngreso: "Observaciones",
    tipoEntrada: "Tipo ingreso",
    tipoDocumento: "Tipo documento",
    numeroIdentificacion: "N° Identificación",

  };

  filter: string = '';
  centros: JerarquiaDTO[] = [];
  filtroCentro: string = '';

  @ViewChild(MatPaginator) paginator: MatPaginator;

  ingresosPaginacion: PaginacionRequest = new PaginacionRequest();

  constructor(private authSerguridadServicio: AuthSerguridadServicio,
    private fichaIdentificacionService: FichaIdentificacionService,
    private dialogMensajeService: DialogMensajeService,
    private accionesSheet: MatBottomSheet,
    private router: Router,
    private route: ActivatedRoute,
    private jerarquiaService: JerarquiaService,
    private funcionesUtils: FuncionesUtils,) {

  }

  ngOnInit(): void {
    this.obtenerFichasIdentificacion();
    this.cargarCentro();

  }

  obtenerFichasIdentificacion() {
    let paginacionRequest = new PaginacionRequestFichaIdentificacion();
    paginacionRequest.size = this.size;
    paginacionRequest.page = this.page;
    paginacionRequest.sort = this.ingresosPaginacion.sort;
    paginacionRequest.direction = this.ingresosPaginacion.direction;
    paginacionRequest.todosEstados = false;
    paginacionRequest.postEgreso = false;
    if (this.filter.length > 0) {
      paginacionRequest.filter = this.filter;
      paginacionRequest.page = 0;
      this.page = 0;
    }
    if (this.filtroCentro.length > 0 && this.filtroCentro != '0') {
      paginacionRequest.tokenCentro = this.filtroCentro;
    }

    this.fichaIdentificacionService.obtenerFichasIdentificacionPaginado(paginacionRequest, etiquetasModel.NEMONICO_MENU_NUEVO_INGRESO).subscribe(
      {
        next: (response: RespuestaPorDefecto<PaginacionResponse<FichaIdentificacionDTO>>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            return;
          }

          this.listaFichasIdentificacion = response.data.data;
          this.dataSource = this.listaFichasIdentificacion;
          this.totalItems = response.data.totalItems;
        },
        error: (error: any) => {
          this.authSerguridadServicio.checkError(error);
        }
      }
    );
  }



  agregarFichaIdentificacion() {
    this.router.navigate(['crear-editar'], { relativeTo: this.route });
  }

  handlePageEvent(pageEvent: PageEvent) {
    this.size = pageEvent.pageSize;
    this.page = pageEvent.pageIndex;
    this.obtenerFichasIdentificacion();
  }

  getKeys() {
    return Object.keys(this.keyLabelsTable);
  }

  editarFichaIdentificacion(fichaIdentificacionDTO: FichaIdentificacionDTO) {
    this.router.navigate(['crear-editar/' + fichaIdentificacionDTO.tokenIdentificador], { state: { fichaIdentificacionDTO }, relativeTo: this.route });
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

          this.centro = respuesta.data;
          this.nombreCentro = this.centro.nombre;
          console.log('centro', this.centro)
          if (this.centro.nemonico == 'SOA' || this.centro.nemonico == 'CJDR') {
            this.cargarCentros();
          }
        },
        error: (error: any) => {
          this.jerarquiaService.checkError(error);
        },
      });
  }

  cargarCentros(): void {

    this.jerarquiaService
      .obtenerJerarquiasPorJerarquiaPadreFuncionario(this.nemonicoMenu)
      .subscribe({
        next: (resp: RespuestaPorDefecto<JerarquiaDTO[]>) => {
          if (resp.exito) {
            this.centros = resp.data;
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

  onChangeCentro(nuevoCentroId: string): void {
    this.filtroCentro = nuevoCentroId;
    console.log('Filtro de centro cambiado a:', this.filtroCentro);
  }


  onXLSX() {
    // Primero, transformamos los datos para que las claves se mapeen a los títulos definidos en keyLabels
    const transformedData = this.listaFichasIdentificacion.map((item, i) => {
      const transformedItem: any = {};

      // Recorremos las claves de keyLabels y excluimos 'acciones'
      Object.keys(this.keyLabelsTable).forEach(key => {
        if (key !== 'acciones') { // Excluir 'acciones'
          const label = this.keyLabelsTable[key] || key; // Si no hay un label, usamos la clave original

          // Manejo de campos especiales
          if (key === 'numero') {
            transformedItem[label] = this.totalItems - (i + (this.page * this.size));
          } else if (key.includes('fecha')) {
            transformedItem[label] = this.getLocalDate(item[key]);
          } else if (key.includes('apellidos')) {
            transformedItem[label] = item['apellidoPaterno'] + ' ' + item['apellidoMaterno'];
          } else if (key.includes('numeroIdentificacion')) {
            transformedItem[label] = item['numeroDocumento'];
          } else if (key.includes('tipoDocumento')) {
            transformedItem[label] = item['nombreTipoDocumento'];
          } else {
            transformedItem[label] = item[key];
          }
        }
      });

      return transformedItem;
    });
    // Creamos la hoja de trabajo con los datos transformados, excluyendo la columna 'acciones'
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(transformedData, { header: Object.values(this.keyLabelsTable).filter(label => label !== 'Acciones') as string[] });

    // Si deseas personalizar las columnas, por ejemplo, establecer un ancho fijo:
    const wscols = [
      { wch: 10 },
      { wch: 15 },
      { wch: 20 },
      { wch: 10 }
    ];
    ws['!cols'] = wscols;

    // Creamos el libro de trabajo
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Datos'); // Aquí "Datos" es el nombre de la hoja

    // Generamos el archivo XLSX
    const fileName = 'datos.xlsx'; // El nombre del archivo de salida
    XLSX.writeFile(wb, fileName); // Descarga el archivo
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
}
