import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { Paginacion } from 'app/core/model/both/paginacion.model';
import * as XLSX from 'xlsx';
import moment from 'moment';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PermisoRolUsuarioService } from 'app/modules/seguridad/services/permiso-rol-usuario.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-tabla-datos',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    MatIconModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatFormFieldModule
  ],
  templateUrl: './tabla-datos.component.html',
  styleUrl: './tabla-datos.component.scss'
})
export class TablaDatosComponent<T> implements OnChanges {
  private permisosService = inject(PermisoRolUsuarioService);      
  private route = inject(ActivatedRoute);      

  @Input() titulo: string = "";
  @Input() listaDatos: T[] = [];
  @Input() etiquetasColumnas: { [key: string]: string } = {};
  @Input() paginacion: Paginacion = new Paginacion();
  @Input() permitirAgregar: boolean = true;
  @Input() permitirVisualizar: boolean = true;
  @Input() permitirEditar: boolean = true;
  @Input() permitirEliminar: boolean = true;
  @Input() permitirBloquear: boolean = true;
  @Input() permitirImprimir: boolean = false;
  @Input() permitirExcel: boolean = true;
  @Input() icono: string;
  @Input() textoAyuda: string;
  @Input() icono2: string;
  @Input() textoAyuda2: string;
  @Input() modoEditarVer: boolean = false;
  @Input() campoVisualizar: string;
  @Input() soloFecha: boolean = false;
  
  // Por defecto no mostrar hora en fechas
  @Input() mostrarHoraEnFechas: boolean = false;

  // Configuración para el formateo de textos
  @Input() formatearTextosLargos: boolean = true;
  @Input() umbralTextoLargo: number = 30;
  @Input() anchoMaximoTexto: number = 200;

  // NUEVAS PROPIEDADES: Control de ordenamiento
  @Input() columnasNoOrdenables: string[] = []; // Permite personalizar desde el componente padre
  @Input() permitirOrdenamientoPorDefecto: boolean = true; // Control general de ordenamiento

  // Obtener permisos por acciones si se proporciona un nemónico
  @Input() nemonicoMenu!: string;
  @Input() validarHistorico: boolean = false;

  @Input() ocultarBarraOpciones: boolean = false;

  @Output() refrescar = new EventEmitter<void>();
  @Output() agregar = new EventEmitter<void>();
  @Output() exportarPdf = new EventEmitter<void>();
  @Output() exportarExcel = new EventEmitter<void>();
  @Output() ver = new EventEmitter<T>();
  @Output() editar = new EventEmitter<T>();
  @Output() eliminar = new EventEmitter<T>();
  @Output() bloquear = new EventEmitter<T>();
  @Output() buscar = new EventEmitter<string>();
  @Output() cambioOrden = new EventEmitter<Sort>();
  @Output() cambioPagina = new EventEmitter<PageEvent>();
  @Output() accionPersonalizada = new EventEmitter<T>();
  @Output() accionPersonalizada2 = new EventEmitter<T>();
  @Output() imprimir = new EventEmitter<T>();
  @Output() requestAllData = new EventEmitter<void>();

  fuenteDatos: CdkTableDataSourceInput<T>;
  terminoBusqueda: string = '';

  etiquetaEditar = etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR;
  etiquetaEliminar = etiquetasModel.ACCIONES_MENU_PERMISO_ELIMINAR;

  /**
   * Inicializa el componente configurando la fuente de datos
   */
  ngOnInit() {
    this.obtenerPermisos();

    this.fuenteDatos = this.listaDatos;
  }

  /**
   * Maneja cambios en las propiedades de entrada
   * @param cambios Cambios detectados en las propiedades
   */
  ngOnChanges(cambios: SimpleChanges) {
    if (cambios['listaDatos'] && cambios['listaDatos'].currentValue) {
      this.fuenteDatos = this.listaDatos;
    }
  }

  /**
   * MÉTODO PRINCIPAL: Determina si una columna puede ser ordenada
   * @param clave Nombre de la columna
   * @returns true si la columna se puede ordenar
   */
  esColumnaOrdenable(clave: string): boolean {
    // Si el ordenamiento está deshabilitado globalmente
    if (!this.permitirOrdenamientoPorDefecto) {
      return false;
    }

    // Columnas que NUNCA se pueden ordenar (por defecto)
    const columnasNoOrdenablesPorDefecto = [
      'acciones',  // Contiene botones, no datos ordenables
      'numero'     // Es un número calculado basado en la posición, no en datos reales
    ];

    // Combinar columnas por defecto con las personalizadas desde el componente padre
    const todasLasColumnasNoOrdenables = [
      ...columnasNoOrdenablesPorDefecto,
      ...this.columnasNoOrdenables
    ];

    // La columna es ordenable si NO está en la lista de no ordenables
    return !todasLasColumnasNoOrdenables.includes(clave);
  }

  /**
   * Maneja cambios en el ordenamiento de la tabla
   * CON VALIDACIÓN ADICIONAL para evitar errores
   * @param evento Evento de ordenamiento
   */
  alCambiarOrden(evento: Sort) {
    // Validar que la columna sea ordenable antes de emitir el evento
    if (!this.esColumnaOrdenable(evento.active)) {
      console.warn(`Intento de ordenar por columna no ordenable: ${evento.active}`);
      return; // No emitir el evento si la columna no es ordenable
    }

    // Solo emitir el evento si la columna es válida
    this.cambioOrden.emit(evento);
  }

  /**
   * Emite evento para solicitar todos los datos para exportación
   */
  onRequestAllData() {
    this.requestAllData.emit();
  }

  /**
   * Exporta datos a formato Excel con formateo apropiado
   * @param data Datos a exportar
   */
  exportXLSX(data: T[]) {
    // Transforma los datos para el Excel
    const transformedData = data.map((item, i) => {
      const transformedItem: any = {};

      Object.keys(this.etiquetasColumnas).forEach(key => {
        if (key !== 'acciones') {
          const label = this.etiquetasColumnas[key] || key;

          if (key === 'numero') {
            transformedItem[label] = this.paginacion.totalItems - (i + (this.paginacion.pageIndex * this.paginacion.pageSize));
          } else if (key.includes('fecha')) {
            // Si el valor ya es string (pre-formateado), usarlo directamente
            if (typeof item[key] === 'string') {
              transformedItem[label] = item[key];
            } else {
              // Si es Date, formatearlo según la configuración
              transformedItem[label] = this.formatearFechaParaExcel(item[key]);
            }
          } else if (typeof item[key] === 'boolean') {
            transformedItem[label] = item[key] ? 'Sí' : 'No';
          } else {
            transformedItem[label] = item[key];
          }
        }
      });

      return transformedItem;
    });

    // Crea la hoja de Excel
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet([]);

    // Agrega título
    const title = "Listado de " + this.titulo;
    ws['A1'] = { t: 's', v: title };
    ws['!merges'] = [{ s: { r: 0, c: 0 }, e: { r: 0, c: Object.keys(this.etiquetasColumnas).length - 1 } }];
    ws['A1'].s = { font: { bold: true, sz: 14 } };

    // Agrega encabezados
    const headers = Object.values(this.etiquetasColumnas).filter(label => label !== 'Acciones');
    XLSX.utils.sheet_add_aoa(ws, [headers], { origin: 'A2' });

    headers.forEach((_, index) => {
      const cellAddress = XLSX.utils.encode_cell({ r: 1, c: index });
      if (!ws[cellAddress]) ws[cellAddress] = { t: 's', v: headers[index] };
      ws[cellAddress].s = { font: { bold: true } };
    });

    // Agrega datos
    XLSX.utils.sheet_add_json(ws, transformedData, { origin: 'A3', skipHeader: true });

    // Define anchos de columnas
    ws['!cols'] = [
      { wch: 20 },
      { wch: 25 },
      { wch: 30 },
      { wch: 20 }
    ];

    // Crea y guarda el archivo
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Datos');
    XLSX.writeFile(wb, `${this.titulo}.xlsx`);
  }

  /**
   * Ejecuta búsqueda con el término ingresado
   */
  alBuscar() {
    this.buscar.emit(this.terminoBusqueda);
  }

  /**
   * Emite evento para refrescar los datos
   */
  alRefrescar() {
    this.refrescar.emit();
  }

  /**
   * Emite evento para agregar nuevo elemento
   */
  alAgregar() {
    this.agregar.emit();
  }

  /**
   * Emite evento para exportar a PDF
   */
  alExportarPDF() {
    this.exportarPdf.emit();
  }

  /**
   * Emite evento para ver detalles de una fila
   * @param fila Elemento a visualizar
   */
  verFila(fila: T) {
    this.ver.emit(fila);
  }

  /**
   * Emite evento para editar una fila
   * @param fila Elemento a editar
   */
  editarFila(fila: T) {
    this.editar.emit(fila);
  }

  /**
   * Emite evento para eliminar una fila
   * @param fila Elemento a eliminar
   */
  eliminarFila(fila: T) {
    this.eliminar.emit(fila);
  }

  /**
   * Emite evento para bloquear una fila
   * @param fila Elemento a bloquear
   */
  bloquearFila(fila: T) {
    this.bloquear.emit(fila);
  }

  /**
   * Ejecuta acción personalizada sobre una fila
   * @param fila Elemento sobre el cual ejecutar la acción
   */
  ejecutarAccion(fila: T) {
    this.accionPersonalizada.emit(fila);
  }

  /**
   * Ejecuta segunda acción personalizada sobre una fila
   * @param fila Elemento sobre el cual ejecutar la acción
   */
  ejecutarAccion2(fila: T) {
    this.accionPersonalizada2.emit(fila);
  }

  /**
   * Emite evento para imprimir una fila
   * @param fila Elemento a imprimir
   */
  imprimirFila(fila: T) {
    this.imprimir.emit(fila);
  }

  /**
   * Maneja cambios en la paginación
   * @param evento Evento de cambio de página
   */
  alCambiarPagina(evento: PageEvent) {
    this.cambioPagina.emit(evento);
  }

  /**
   * Obtiene las claves de las columnas a mostrar
   * @returns Array de nombres de columnas
   */
  obtenerClaves() {
    return Object.keys(this.etiquetasColumnas);
  }

  /**
   * Formatea fecha para mostrar en la tabla según configuración
   * @param fecha Fecha a formatear
   * @returns Fecha formateada como string
   */
  obtenerFechaLocal(fecha: Date | string): string {
    if (!fecha) return '';
    
    // Si ya es string (pre-formateado), devolverlo tal como está
    if (typeof fecha === 'string') {
      return fecha;
    }
    
    // Por defecto mostrar solo fecha (DD-MM-YYYY), solo si mostrarHoraEnFechas es true mostrar hora
    if (this.mostrarHoraEnFechas) {
      return moment(fecha).format("DD-MM-YYYY HH:mm:ss");
    } else {
      // Por defecto mostrar solo fecha
      return moment(fecha).format("DD-MM-YYYY");
    }
  }

  /**
   * Formatea fecha para mostrar solo día-mes-año
   * @param fecha Fecha a formatear
   * @returns Fecha en formato DD-MM-YYYY
   */
  obtenerFechaLocalSoloFecha(fecha: Date | string): string {
    if (!fecha) return '';
    
    // Si ya es string (pre-formateado), devolverlo tal como está
    if (typeof fecha === 'string') {
      return fecha;
    }
    
    return moment(fecha).format("DD-MM-YYYY");
  }

  /**
   * Formatea fecha específicamente para exportación Excel
   * @param fecha Fecha a formatear
   * @returns Fecha formateada según configuración
   */
  private formatearFechaParaExcel(fecha: Date | string): string {
    if (!fecha) return '';
    
    // Si ya es string (pre-formateado), devolverlo tal como está
    if (typeof fecha === 'string') {
      return fecha;
    }
    
    // Para Excel, usar la misma lógica que para la tabla (por defecto sin hora)
    if (this.mostrarHoraEnFechas) {
      return moment(fecha).format("DD-MM-YYYY HH:mm:ss");
    } else {
      return moment(fecha).format("DD-MM-YYYY");
    }
  }

  /**
   * Determina si un valor debe mostrarse con formato de texto largo
   * @param valor Valor a evaluar
   * @returns true si debe aplicarse formato de texto largo
   */
  esTextoLargo(valor: any): boolean {
    if (!this.formatearTextosLargos) return false;
    return typeof valor === 'string' && valor?.length > this.umbralTextoLargo;
  }

  /**
   * Devuelve estilos CSS para aplicar a textos largos
   * @returns Objeto con estilos CSS
   */
  getEstiloTextoLargo(): { [key: string]: string } {
    return {
      'white-space': 'normal',
      'word-break': 'break-word',
      'max-width': `${this.anchoMaximoTexto}px`,
      'overflow': 'hidden'
    };
  }

  /**
   * MÉTODO DE DEBUG: Muestra información sobre ordenamiento
   */
  debugOrdenamiento(): void {
    console.group('🔍 DEBUG ORDENAMIENTO TABLA-DATOS');
    console.log('Columnas disponibles:', Object.keys(this.etiquetasColumnas));
    console.log('Columnas no ordenables (por defecto):', ['acciones', 'numero']);
    console.log('Columnas no ordenables (personalizadas):', this.columnasNoOrdenables);
    console.log('Permitir ordenamiento por defecto:', this.permitirOrdenamientoPorDefecto);
    
    Object.keys(this.etiquetasColumnas).forEach(clave => {
      console.log(`- ${clave}: ${this.esColumnaOrdenable(clave) ? '✅ Ordenable' : '❌ No ordenable'}`);
    });
    console.groupEnd();
  }

  /**
   * Obtiene permisos de acciones basados en el nemónico proporcionado
   */
  obtenerPermisos() {
    if (!this.nemonicoMenu) {
      return;
    }

    this.permitirAgregar = this.permisosService.hasPermission(
      this.nemonicoMenu,
      etiquetasModel.ACCIONES_MENU_PERMISO_AGREGAR
    );

    // this.permisosService.obtenerPermisosUsuario(this.nemonicoMenu, uuid).subscribe(() => {
  
    //   this.permitirVisualizar = this.permisosService.hasPermission(
    //     this.nemonicoMenu,
    //     etiquetasModel.ACCIONES_MENU_PERMISO_VER
    //   );
  
    //   this.permitirEditar = this.permisosService.hasPermission(
    //     this.nemonicoMenu,
    //     etiquetasModel.ACCIONES_MENU_PERMISO_EDITAR
    //   );
  
    //   this.permitirEliminar = this.permisosService.hasPermission(
    //     this.nemonicoMenu,
    //     etiquetasModel.ACCIONES_MENU_PERMISO_ELIMINAR
    //   );
    // });

  }
}
