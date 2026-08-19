import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectionListChange } from '@angular/material/list';
import { FormsModule } from '@angular/forms';
import etiquetasModel from 'app/core/etiquetas.model';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { ExportacionRequest } from 'app/core/model/request/ExportacionRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { ReporteService } from 'app/modules/seguridad/services/reporte.service';

interface AdolescenteOpcion {
  tokenIdentificador: string;
  etiqueta: string;
  ficha: FichaIdentificacionDTO;
}

interface MenuOpcion {
  nemonico: string;
  etiqueta: string;
}

@Component({
  selector: 'app-informacion-adolescentes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
  ],
  templateUrl: './informacion-adolescentes.component.html',
  styleUrl: './informacion-adolescentes.component.scss'
})
export class InformacionAdolescentesComponent implements OnInit {
  private reporteService = inject(ReporteService);
  private backendService = inject(BackendService);
  private dialogMensajeService = inject(DialogMensajeService);
  private fichaIdentificacionService = inject(FichaIdentificacionService);

  nemonicoMenu = etiquetasModel.NEMONICO_REPORTE_ADOLESCENTES_EXTERNADOS;
  exportando = false;
  filtroCentro = '';

  busquedaAdolescentes = '';
  adolescentesDisponibles: AdolescenteOpcion[] = [];
  adolescentesFiltrados: AdolescenteOpcion[] = [];
  adolescentesSeleccionados = new Set<string>();
  cargandoAdolescentes = false;
  mensajeAdolescentes = '';

  busquedaMenus = '';
  menusDisponibles: MenuOpcion[] = [];
  menusFiltrados: MenuOpcion[] = [];
  menusSeleccionados = new Set<string>();

  ngOnInit(): void {
    this.inicializarMenus();
    this.cargarAdolescentesPorJerarquia();
  }

  private inicializarMenus(): void {
    this.menusDisponibles = [
      {
        nemonico: 'SECCION_FICHA_IDENT_FICHA_PRINCIPAL',
        etiqueta: 'Ficha principal',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_INGRESO',
        etiqueta: 'Ficha ingreso',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EXPEDIENTE_MATRIZ',
        etiqueta: 'Expedientes legales',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EXPEDIENTE_MATRIZ_ACTAS_EXTERNAMIENTO',
        etiqueta: 'Expedientes legales - Actas de externamiento',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_ENTREGA_RETIRO_DE_PERTENENCIAS',
        etiqueta: 'Entrega/Retiro de pertenencias',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL',
        etiqueta: 'Ficha psicosocial - Composición familiar',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL_SITUACION_ECONOMICA',
        etiqueta: 'Ficha psicosocial - Situación económica',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL_SITUACION_EDUCATIVA',
        etiqueta: 'Ficha psicosocial - Situación educativa',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_FICHA_PSICOSOCIAL_SITUACION_RIESGO_SOCIAL',
        etiqueta: 'Ficha psicosocial - Situación de riesgo social',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EVALUACION_SOCIAL_DOMICILIARIA',
        etiqueta: 'Evaluación social - Evaluación domiciliaria',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EVALUACION_SOCIAL_ORIENTACION_CONSEJERIA_FAMILIAR',
        etiqueta: 'Evaluación social - Orientación y consejería familiar',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EVALUACION_SOCIAL_SEGUIMIENTO_SOCIAL',
        etiqueta: 'Evaluación social - Seguimiento social',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EVALUACION_SOCIAL_AUTORIZACION_VISITANTES',
        etiqueta: 'Evaluación social - Autorización de visitantes',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_HISTORIA_CLINICA',
        etiqueta: 'Historia clínica',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EVALUACIONES_PSICOLOGICAS',
        etiqueta: 'Evaluaciones psicológicas',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_NIVEL_RIESGO',
        etiqueta: 'Valoración de nivel de riesgo',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_EVALUACION_CONDUCTUAL',
        etiqueta: 'Evaluación conductual',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_PLAN_TRATAMIENTO',
        etiqueta: 'Plan de tratamiento individual',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_INFORMES',
        etiqueta: 'Informes',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_PERMISO_SALIDA',
        etiqueta: 'Permiso de salida',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_SANCIONES_DISCIPLINARIAS',
        etiqueta: 'Sanciones disciplinarias',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_PROGRAMA_INTERVENCION_INTENSIVA',
        etiqueta: 'Programa de intervención intensiva',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_NOTIFICACIONES',
        etiqueta: 'Notificaciones',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_PREPARACION_EGRESO',
        etiqueta: 'Preparación para el Egreso',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_INFORME_FINAL',
        etiqueta: 'Informe final',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_POST_EGRESO',
        etiqueta: 'Post egreso',
      },
      {
        nemonico: 'SECCION_FICHA_IDENT_FLUJOS',
        etiqueta: 'Flujos',
      }      
    ];
    this.aplicarFiltroMenus();
  }

  private cargarAdolescentesPorJerarquia(): void {
    this.filtroCentro = localStorage.getItem('jerarquiaIdentificador') || '';
    if (!this.filtroCentro) {
      this.mensajeAdolescentes = 'No se encontro jerarquiaIdentificador en localStorage.';
      this.adolescentesDisponibles = [];
      this.adolescentesFiltrados = [];
      this.adolescentesSeleccionados.clear();
      return;
    }

    this.cargandoAdolescentes = true;
    this.mensajeAdolescentes = '';

    this.fichaIdentificacionService.obtenerNombresFichas(null, this.filtroCentro).subscribe({
      next: (response) => {
        const adolescentes = (response?.data || [])
          .sort((a, b) =>
            (a.apellidoPaterno || '').toLowerCase().localeCompare((b.apellidoPaterno || '').toLowerCase())
          );

        this.adolescentesDisponibles = adolescentes.map((ficha) => ({
          tokenIdentificador: ficha.tokenIdentificador,
          etiqueta: this.obtenerEtiquetaAdolescente(ficha),
          ficha,
        }));

        this.aplicarFiltroAdolescentes();
        if (this.adolescentesDisponibles.length === 0) {
          this.mensajeAdolescentes = 'No se encontraron adolescentes para la jerarquia seleccionada.';
        }
      },
      error: (error: any) => {
        this.fichaIdentificacionService.checkError(error);
        this.adolescentesDisponibles = [];
        this.adolescentesFiltrados = [];
        this.adolescentesSeleccionados.clear();
        this.mensajeAdolescentes = 'No fue posible cargar la lista de adolescentes.';
      },
      complete: () => {
        this.cargandoAdolescentes = false;
      },
    });
  }

  private obtenerEtiquetaAdolescente(ficha: FichaIdentificacionDTO): string {
    const numeroIdentificacion = ficha.numeroIdentificacion || 'Sin identificacion';
    const nombreCompleto = [ficha.apellidoPaterno, ficha.apellidoMaterno, ficha.nombres]
      .filter(Boolean)
      .join(' ')
      .trim();

    return `${numeroIdentificacion} - ${nombreCompleto || 'Sin nombres'}`;
  }

  aplicarFiltroAdolescentes(): void {
    const termino = (this.busquedaAdolescentes || '').trim().toLowerCase();
    this.adolescentesFiltrados = !termino
      ? [...this.adolescentesDisponibles]
      : this.adolescentesDisponibles.filter((adolescente) =>
          adolescente.etiqueta.toLowerCase().includes(termino)
        );
  }

  aplicarFiltroMenus(): void {
    const termino = (this.busquedaMenus || '').trim().toLowerCase();
    this.menusFiltrados = !termino
      ? [...this.menusDisponibles]
      : this.menusDisponibles.filter((menu) => menu.etiqueta.toLowerCase().includes(termino));
  }

  onBusquedaAdolescentesChange(): void {
    this.aplicarFiltroAdolescentes();
  }

  onBusquedaMenusChange(): void {
    this.aplicarFiltroMenus();
  }

  onSeleccionAdolescenteLista(event: MatSelectionListChange): void {
    event.options.forEach((option) => {
      const tokenIdentificador = option.value as string;

      if (option.selected) {
        this.adolescentesSeleccionados.add(tokenIdentificador);
      } else {
        this.adolescentesSeleccionados.delete(tokenIdentificador);
      }
    });
  }

  onSeleccionMenuLista(event: MatSelectionListChange): void {
    event.options.forEach((option) => {
      const nemonico = option.value as string;

      if (option.selected) {
        this.menusSeleccionados.add(nemonico);
      } else {
        this.menusSeleccionados.delete(nemonico);
      }
    });
  }

  trackByAdolescente(index: number, adolescente: AdolescenteOpcion): string {
    return adolescente.tokenIdentificador;
  }

  trackByMenu(index: number, menu: MenuOpcion): string {
    return menu.nemonico;
  }

  seleccionarTodosAdolescentes(seleccionar: boolean): void {
    this.adolescentesFiltrados.forEach((adolescente) => {
      if (seleccionar) {
        this.adolescentesSeleccionados.add(adolescente.tokenIdentificador);
      } else {
        this.adolescentesSeleccionados.delete(adolescente.tokenIdentificador);
      }
    });
  }

  seleccionarTodosMenus(seleccionar: boolean): void {
    this.menusFiltrados.forEach((menu) => {
      if (seleccionar) {
        this.menusSeleccionados.add(menu.nemonico);
      } else {
        this.menusSeleccionados.delete(menu.nemonico);
      }
    });
  }

  estaSeleccionadoAdolescente(tokenIdentificador: string): boolean {
    return this.adolescentesSeleccionados.has(tokenIdentificador);
  }

  estaSeleccionadoMenu(nemonico: string): boolean {
    return this.menusSeleccionados.has(nemonico);
  }

  get totalAdolescentesSeleccionados(): number {
    return this.adolescentesSeleccionados.size;
  }

  get totalMenusSeleccionados(): number {
    return this.menusSeleccionados.size;
  }

  get todosAdolescentesFiltradosSeleccionados(): boolean {
    return this.adolescentesFiltrados.length > 0
      && this.adolescentesFiltrados.every((adolescente) =>
        this.adolescentesSeleccionados.has(adolescente.tokenIdentificador)
      );
  }

  get algunAdolescenteFiltradoSeleccionado(): boolean {
    return this.adolescentesFiltrados.some((adolescente) =>
      this.adolescentesSeleccionados.has(adolescente.tokenIdentificador)
    );
  }

  get todosMenusFiltradosSeleccionados(): boolean {
    return this.menusFiltrados.length > 0
      && this.menusFiltrados.every((menu) => this.menusSeleccionados.has(menu.nemonico));
  }

  get algunMenuFiltradoSeleccionado(): boolean {
    return this.menusFiltrados.some((menu) => this.menusSeleccionados.has(menu.nemonico));
  }

  exportarAdolescentes(): void {
    if (this.exportando) {
      return;
    }

    this.exportando = true;
    if (this.debeMostrarAdvertenciaExportacion()) {
      this.mostrarAdvertenciaExportacion();
      return;
    }

    this.ejecutarExportacion();
  }

  private debeMostrarAdvertenciaExportacion(): boolean {
    return this.totalAdolescentesSeleccionados > 1 || this.totalMenusSeleccionados > 1;
  }

  private mostrarAdvertenciaExportacion(): void {
    const titulo = 'Advertencia de exportación';
    const mensaje = 'Tome en cuenta que al exportar múltiples adolescentes y secciones la acción podría tomar tiempo y/o <strong>generar errores</strong>. Además, la exportación de múltiples secciones puede generar información de difícil interpretación.';

    this.dialogMensajeService.mensajeConConfirmacion(titulo, mensaje).afterClosed().subscribe({
      next: (resp: 'confirmed' | 'cancelled') => {
        if (resp === 'confirmed') {
          this.ejecutarExportacion();
          return;
        }

        this.exportando = false;
      },
      error: () => {
        this.exportando = false;
      },
    });
  }

  private ejecutarExportacion(): void {
    const payload = this.obtenerPayloadExportacion();

    this.reporteService.exportarAdolescentes(payload, this.nemonicoMenu).subscribe({
      next: async (arrayBuffer: ArrayBuffer) => {
        const fueError = await this.manejarRespuestaEncriptadaError(arrayBuffer);
        if (!fueError) {
          this.descargarCsv(arrayBuffer);
        }
      },
      error: async (error: any) => {
        const arrayBuffer = error?.error as ArrayBuffer;
        const fueErrorEncriptado = await this.manejarRespuestaEncriptadaError(arrayBuffer);
        if (!fueErrorEncriptado) {
          await this.reporteService.checkError(error);
        }
      },
      complete: () => {
        this.exportando = false;
      }
    });
  }

  private obtenerPayloadExportacion(): ExportacionRequest {
    const numerosIdentificacion = this.adolescentesDisponibles
      .filter((adolescente) => this.adolescentesSeleccionados.has(adolescente.tokenIdentificador))
      .map((adolescente) => (adolescente.ficha.numeroIdentificacion || '').trim())
      .filter((numeroIdentificacion) => numeroIdentificacion.length > 0);

    const payload = new ExportacionRequest();
    payload.numerosIdentificacion = Array.from(new Set(numerosIdentificacion));
    payload.nemonicosSecciones = Array.from(new Set(this.menusSeleccionados));

    return payload;
  }

  private descargarCsv(arrayBuffer: ArrayBuffer): void {
    const csvTexto = this.decodificarCsv(arrayBuffer);
    const csvConBom = `\uFEFF${csvTexto}`;
    const csvBlob = new Blob([csvConBom], { type: 'text/csv;charset=utf-8' });
    const url = window.URL.createObjectURL(csvBlob);
    const link = document.createElement('a');

    link.href = url;
    link.download = 'adolescentes.csv';
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }

  private decodificarCsv(arrayBuffer: ArrayBuffer): string {
    const bytes = new Uint8Array(arrayBuffer);

    try {
      return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    } catch {
      // Fallback para respuestas legacy que pueden venir en ANSI/Windows-1252.
      return new TextDecoder('windows-1252').decode(bytes);
    }
  }

  private async manejarRespuestaEncriptadaError(arrayBuffer?: ArrayBuffer): Promise<boolean> {
    if (!arrayBuffer || arrayBuffer.byteLength === 0) {
      return false;
    }

    try {
      const textoRespuesta = new TextDecoder('utf-8').decode(new Uint8Array(arrayBuffer));
      const bodyEncriptado = JSON.parse(textoRespuesta) as BodyEncriptado;

      if (!bodyEncriptado?.body || !bodyEncriptado?.llave) {
        return false;
      }

      const respuesta = await this.backendService.desencriptarBdyEncriptado<RespuestaPorDefecto<any>>(bodyEncriptado);
      const titulo = respuesta?.titulo || 'Petición fallida';
      const mensaje = respuesta?.mensaje || 'No fue posible exportar la información de adolescentes';

      this.dialogMensajeService.mensajeErrorConTitulo(titulo, mensaje);
      return true;
    } catch {
      return false;
    }
  }
}
