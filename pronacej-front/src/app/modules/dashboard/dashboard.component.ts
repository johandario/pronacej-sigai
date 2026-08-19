import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import {
    ApexAxisChartSeries,
    ApexChart,
    ApexDataLabels,
    ApexNonAxisChartSeries,
    ApexPlotOptions,
    ApexTitleSubtitle,
    ApexXAxis,
    ApexYAxis,
    NgApexchartsModule,
} from 'ng-apexcharts';
import etiquetasModel from 'app/core/etiquetas.model';
import { EstadisticaItemDTO } from 'app/core/model/both/EstadisticaItemDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DashboardCentroDTO } from 'app/core/model/both/DashboardCentroDTO.model';
import { DashboardEstadisticasDTO } from 'app/core/model/both/DashboardEstadisticasDTO.model';
import { DashboardRequest } from 'app/core/model/both/DashboardRequest.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { DashboardService } from './services/dashboard.service';
import { FuncionarioService } from '../seguridad/services/funcionario.service';
import { AuthSerguridadServicio } from '../seguridad/services/auth.seguridad.service';
import { JerarquiaService } from '../seguridad/services/jerarquia.service';

type ClaveEstadistica 
= 'porDelito' 
| 'porEdad' 
| 'porSexo' 
| 'porNacionalidad' 
| 'porDepartamento' 
| 'porDiasInternacion' 
| 'porTipoEnfermedad' 
| 'porGradoInstruccion' 
| 'porNumeroHijos'
| 'porNumeroCentros';

type TipoWidget = 'bar' | 'line' | 'donut' | 'card';

type ConfiguracionWidget = {
    clave: ClaveEstadistica;
    etiqueta: string;
    subtitulo: string;
    tipo: TipoWidget;
    orientacion?: 'horizontal' | 'vertical';
    altura?: number;
    ancho?: number;
    colores?: string[];
};

type DashboardWidgetChartOptions = {
    series: ApexAxisChartSeries | ApexNonAxisChartSeries;
    chart: ApexChart;
    xaxis?: ApexXAxis;
    yaxis?: ApexYAxis;
    title?: ApexTitleSubtitle;
    dataLabels?: ApexDataLabels;
    plotOptions?: ApexPlotOptions;
    labels?: string[];
    colors?: string[];
    legend?: Record<string, unknown>;
    stroke?: Record<string, unknown>;
    tooltip?: Record<string, unknown>;
};

type WidgetDashboard = ConfiguracionWidget & {
    items: EstadisticaItemDTO[];
    total: number;
    resumen: string;
    vacio: boolean;
    chartOptions?: Partial<DashboardWidgetChartOptions>;
};

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatIconModule,
        MatSelectModule,
        NgApexchartsModule,
    ],
    templateUrl: './dashboard.component.html',
    styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
    nemonicoMenu = etiquetasModel.NEMONICO_MENU_REPORTES_ESTADISTICOS;
    readonly llaveJerarquiaActual = 'jerarquiaIdentificador';

    cargando = false;
    mensajeError = '';
    titulo = 'Dashboard Estadístico';
    subtitulo = '';

    centros: DashboardCentroDTO[] = [];
    centroSeleccionado = '';
    esAdministrador = false;
    funcionarioActivo: FuncionarioDTO | null = null;

    estadisticas: DashboardEstadisticasDTO | null = null;

    widgetsConfiguracion: ConfiguracionWidget[] = [
        {
            clave: 'porNumeroCentros',
            etiqueta: 'Cantidad de centros por tipo',
            subtitulo: 'Distribución por tipo de centro',
            tipo: 'card',
            ancho: 4
        },
        {
            clave: 'porDelito',
            etiqueta: 'Cantidad de adolescentes por delito',
            subtitulo: 'Distribución por delito',
            tipo: 'bar',
            orientacion: 'horizontal',
            ancho: 4,
        },
        {
            clave: 'porEdad',
            etiqueta: 'Cantidad de adolescentes por edad',
            subtitulo: 'Distribución por edad',
            tipo: 'donut',
            ancho: 2,
        },
        {
            clave: 'porSexo',
            etiqueta: 'Cantidad de adolescentes por sexo',
            subtitulo: 'Distribución por sexo',
            tipo: 'donut',
            ancho: 2,
        },
        {
            clave: 'porNacionalidad',
            etiqueta: 'Cantidad de adolescentes por nacionalidad',
            subtitulo: 'Distribución por nacionalidad',
            tipo: 'bar',
            orientacion: 'vertical',
            ancho: 4,
        },
        {
            clave: 'porDepartamento',
            etiqueta: 'Cantidad de adolescentes por departamento',
            subtitulo: 'Distribución por departamento',
            tipo: 'bar',
            orientacion: 'vertical',
            ancho: 4,
        },
        {
            clave: 'porDiasInternacion',
            etiqueta: 'Cantidad de adolescentes por días de internación',
            subtitulo: 'Distribución por días de internación',
            tipo: 'bar',
            orientacion: 'horizontal',
            ancho: 4,
            // colores: ['#2563eb', '#0f172a', '#38bdf8', '#7dd3fc'],
        },
        {
            clave: 'porTipoEnfermedad',
            etiqueta: 'Cantidad de adolescentes por tipo de enfermedad',
            subtitulo: 'Distribución por tipo de enfermedad',
            tipo: 'line',
            ancho: 4,
        },
        {
            clave: 'porGradoInstruccion',
            etiqueta: 'Cantidad de adolescentes por grado de instrucción',
            subtitulo: 'Distribución por grado de instrucción',
            tipo: 'bar',
            orientacion: 'horizontal',
            ancho: 4,
        },
        {
            clave: 'porNumeroHijos',
            etiqueta: 'Cantidad de adolescentes por número de hijos',
            subtitulo: 'Distribución por número de hijos',
            tipo: 'donut',
            ancho: 4,
            colores: ['#2563eb', '#0f172a', '#38bdf8', '#7dd3fc'],
        },        
    ];

    widgets: WidgetDashboard[] = [];

    constructor(
        private authSeguridadService: AuthSerguridadServicio,
        private dashboardService: DashboardService,
        private funcionarioService: FuncionarioService,
        private jerarquiaService: JerarquiaService
    ) {}

    async ngOnInit(): Promise<void> {
        await this.authSeguridadService.verificarPermisosPantallaConServicio(this.nemonicoMenu);

        this.actualizarWidgets();
        this.cargarDashboardInicial();
    }

    cargarDashboardInicial(): void {
        this.cargando = true;
        this.mensajeError = '';

        this.funcionarioService
            .obtenerFuncionarioDelUsuario(this.nemonicoMenu)
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<FuncionarioDTO>
                ) => {
                    if (response.exito && response.data) {
                        this.funcionarioActivo = response.data;
                        this.esAdministrador = this.funcionarioActivo.cargoSuperRol;
                    } else {
                        this.esAdministrador = false;
                    }

                    this.iniciarCargaSegunRol();
                },
                error: () => {
                    this.esAdministrador = false;
                    this.iniciarCargaSegunRol();
                },
            });
    }

    private iniciarCargaSegunRol(): void {
        if (this.esAdministrador) {
            this.cargarCentros();
            return;
        }       

        this.titulo = this.titulo + (' - ' + this.funcionarioActivo?.departamento || '');

        const tokenCentroActual = this.obtenerTokenCentroActual();

        if (!tokenCentroActual) {
            this.mensajeError =
                'No se encontro el centro actual del usuario para cargar el dashboard.';
            this.cargando = false;
            this.estadisticas = null;
            this.actualizarWidgets();
            return;
        }       

        this.centroSeleccionado = tokenCentroActual;
        this.centros = [];
        this.cargarEstadisticas();
    }

    cargarCentros(): void {
        this.cargando = true;
        this.mensajeError = '';

        this.dashboardService.obtenerCentros(this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<DashboardCentroDTO[]>) => {
                if (!response.exito) {
                    this.mensajeError = response.mensaje || 'No fue posible cargar los centros.';
                    this.cargando = false;
                    return;
                }

                this.centros = response.data || [];
                this.centroSeleccionado = '';
                this.cargarEstadisticas();
            },
            error: () => {
                this.mensajeError = 'No fue posible cargar los centros.';
                this.cargando = false;
            },
        });
    }

    cargarEstadisticas(): void {
        this.cargando = true;
        this.mensajeError = '';

        const request = new DashboardRequest();
        request.tokenCentro = this.centroSeleccionado || null;

        this.dashboardService.obtenerEstadisticas(request, this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<DashboardEstadisticasDTO>) => {
                if (!response.exito) {
                    this.mensajeError = response.mensaje || 'No fue posible cargar las estadísticas.';
                    this.cargando = false;
                    this.estadisticas = null;
                    this.actualizarWidgets();
                    return;
                }

                this.estadisticas = response.data || null;
                this.actualizarWidgets();
                this.cargando = false;
            },
            error: () => {
                this.mensajeError = 'No fue posible cargar las estadísticas.';
                this.cargando = false;
                this.estadisticas = null;
                this.actualizarWidgets();
            },
        });
    }

    cambiarCentro(): void {
        if (!this.esAdministrador) {
            return;
        }

        this.cargarEstadisticas();
    }

    refrescarDashboard(): void {
        this.cargarEstadisticas();
    }

    obtenerValorCentro(centro: DashboardCentroDTO | undefined): string {
        return centro?.tokenIdentificador || '';
    }

    obtenerNombreCentro(centro: DashboardCentroDTO | undefined): string {
        return centro?.nombre || 'Centro sin nombre';
    }

    trackByWidgetClave(_: number, widget: WidgetDashboard): ClaveEstadistica {
        return widget.clave;
    }

    private esCargoAdministrador(cargo: string | null | undefined): boolean {
        return (cargo || '').trim().toLowerCase() === 'administrador';
    }

    private obtenerTokenCentroActual(): string {
        return localStorage.getItem(this.llaveJerarquiaActual) || '';
    }

    private actualizarWidgets(): void {
        this.subtitulo = this.centroSeleccionado
            ? 'Adolescentes del centro seleccionado'
            : 'Adolescentes de todos los centros';

        this.widgets = this.widgetsConfiguracion.map((configuracion) =>
            this.construirWidget(configuracion)
        );
    }

    private construirWidget(configuracion: ConfiguracionWidget): WidgetDashboard {
        const items = this.obtenerItems(configuracion.clave);
        const total = items.reduce((acumulado, item) => acumulado + item.cantidad, 0);

        return {
            ...configuracion,
            items,
            total,
            resumen: this.formatearResumen(items, total, configuracion),
            vacio: items.length === 0,
            chartOptions: this.generarChartOptions(configuracion, items),
        };
    }

    private obtenerItems(clave: ClaveEstadistica): EstadisticaItemDTO[] {
        if (!this.estadisticas) {
            return [];
        }

        const serie = this.estadisticas[clave];

        if (!Array.isArray(serie)) {
            return [];
        }

        return serie
            .map((item) => this.normalizarItem(item))
            .filter((item): item is EstadisticaItemDTO => !!item);
    }

    private generarChartOptions(
        configuracion: ConfiguracionWidget,
        items: EstadisticaItemDTO[]
    ): Partial<DashboardWidgetChartOptions> | undefined {
        if (configuracion.tipo === 'card') {
            return undefined;
        }

        if (configuracion.tipo === 'donut') {
            return {
                series: items.map((item) => item.cantidad),
                chart: {
                    type: 'donut',
                    height: configuracion.altura || 320,
                    toolbar: {
                        show: false,
                    },
                },
                labels: items.map((item) => item.etiqueta),
                colors: configuracion.colores,
                legend: {
                    position: 'bottom',
                },
                dataLabels: {
                    enabled: true,
                },
                title: {
                    text: configuracion.etiqueta,
                    align: 'center',
                },
                tooltip: {
                    enabled: true,
                },
            };
        }

        if (configuracion.tipo === 'bar') {
            const esHorizontal = configuracion.orientacion !== 'vertical';
            const valorMaximo = this.obtenerValorMaximo(items);

            return {
                series: [
                    {
                        name: 'Cantidad',
                        data: items.map((item) => item.cantidad),
                    },
                ],
                chart: {
                    type: 'bar',
                    height: this.calcularAlturaBarra(configuracion, items.length),
                    toolbar: {
                        show: false,
                    },
                },
                plotOptions: {
                    bar: {
                        horizontal: esHorizontal,
                        ...(esHorizontal
                            ? { barHeight: this.calcularAltoFilaBarra(items.length) }
                            : { columnWidth: '60%' }),
                        borderRadius: 8,
                    },
                },
                dataLabels: {
                    enabled: true,
                    formatter: (valor: number) => this.formatearEntero(valor),
                },
                xaxis: esHorizontal
                    ? {
                          categories: items.map((item) => item.etiqueta),
                          min: 0,
                          tickAmount: this.calcularCantidadTicks(valorMaximo),
                          decimalsInFloat: 0,
                          labels: {
                              formatter: (valor: string | number) =>
                                  this.formatearEntero(Number(valor)),
                          },
                      }
                    : {
                          categories: items.map((item) => item.etiqueta),
                          labels: {
                              rotate: -35,
                              trim: true,
                              style: {
                                  fontSize: '11px',
                              },
                          },
                      },
                yaxis: esHorizontal
                    ? {
                          forceNiceScale: true,
                          tickAmount: this.calcularCantidadTicks(valorMaximo),
                          decimalsInFloat: 0,
                          labels: {
                              minWidth: 180,
                              maxWidth: 300,
                              style: {
                                  fontSize: '11px',
                              },
                          },
                      }
                    : {
                          forceNiceScale: true,
                          tickAmount: this.calcularCantidadTicks(valorMaximo),
                          decimalsInFloat: 0,
                          labels: {
                              formatter: (valor: string | number) =>
                                  this.formatearEntero(Number(valor)),
                          },
                      },
                title: {
                    text: configuracion.etiqueta,
                    align: 'center',
                },
                tooltip: {
                    enabled: true,
                    theme: 'dark',
                    y: {
                        formatter: (valor: number) => this.formatearEntero(valor),
                    },
                },
            };
        }

        return {
            series: [
                {
                    name: 'Cantidad',
                    data: items.map((item) => item.cantidad),
                },
            ],
            chart: {
                type: configuracion.tipo,
                height: configuracion.altura || 320,
                toolbar: {
                    show: false,
                },
            },
            dataLabels: {
                enabled: true,
            },
            xaxis: {
                categories: items.map((item) => item.etiqueta),
                labels: {
                    rotate: -35,
                    trim: true,
                },
            },
            yaxis: {
                title: {
                    text: 'Cantidad',
                },
            },
            title: {
                text: configuracion.etiqueta,
                align: 'center',
            },
            tooltip: {
                enabled: true,
                theme: 'dark',
            },
        };
    }

    private calcularAlturaBarra(
        configuracion: ConfiguracionWidget,
        cantidadItems: number
    ): number {
        if (configuracion.altura) {
            return configuracion.altura;
        }

        if (configuracion.orientacion === 'vertical') {
            return 360;
        }

        const alturaBase = 220;
        const alturaPorItem = 34;

        return Math.max(360, alturaBase + cantidadItems * alturaPorItem);
    }

    private calcularAltoFilaBarra(cantidadItems: number): string {
        if (cantidadItems >= 20) {
            return '65%';
        }

        if (cantidadItems >= 12) {
            return '58%';
        }

        return '50%';
    }

    private obtenerValorMaximo(items: EstadisticaItemDTO[]): number {
        return items.reduce((maximo, item) => Math.max(maximo, item.cantidad), 0);
    }

    private calcularCantidadTicks(valorMaximo: number): number {
        if (valorMaximo <= 1) {
            return 2;
        }

        return Math.min(10, valorMaximo + 1);
    }

    private formatearEntero(valor: number): string {
        if (Number.isNaN(valor)) {
            return '0';
        }

        return `${Math.round(valor)}`;
    }

    private normalizarItem(item: any): EstadisticaItemDTO | null {
        if (item == null) {
            return null;
        }

        if (typeof item === 'number') {
            return {
                etiqueta: 'Cantidad',
                cantidad: item,
            };
        }

        if (typeof item === 'string') {
            return {
                etiqueta: item,
                cantidad: 0,
            };
        }

        const etiqueta =
            item.etiqueta ||
            item.nombre ||
            item.label ||
            item.titulo ||
            item.descripcion ||
            item.estado ||
            item.tipo ||
            'Sin nombre';

        const cantidad = Number(
            item.cantidad ?? item.valor ?? item.total ?? item.count ?? item.monto ?? 0
        );

        return {
            etiqueta,
            cantidad: Number.isNaN(cantidad) ? 0 : cantidad,
        };
    }

    private formatearResumen(
        items: EstadisticaItemDTO[],
        total: number,
        configuracion: ConfiguracionWidget
    ): string {
        if (!items.length) {
            return `Sin datos disponibles para ${configuracion.etiqueta.toLowerCase()}.`;
        }

        const detalle = items
            .map((item) => `${item.etiqueta}: ${item.cantidad}`)
            .join(' / ');

        return `${detalle} | Total: ${total}`;
    }
}
