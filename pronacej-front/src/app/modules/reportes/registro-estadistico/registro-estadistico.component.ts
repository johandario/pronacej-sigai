import { CommonModule } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import {
  NgApexchartsModule, ApexChart,
  ApexAxisChartSeries,
  ApexDataLabels,
  ApexTitleSubtitle,
  ApexXAxis,
  ApexYAxis
} from "ng-apexcharts";
import { ExpedienteMatrizService } from '../../seguridad/services/expedienteMatriz.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DelitoEstadisticaDTO } from 'app/core/model/both/DelitoEstadisticoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { MatSelectModule } from '@angular/material/select';
import { FichaIdentificacionService } from '../../administracion/services/fichaIdentificacion.service';
import { EdadEstadisticaDTO } from 'app/core/model/both/EdadEstadisticaDTO.model';
import { JerarquiaService } from '../../seguridad/services/jerarquia.service';
import { FichaCentroEstadisticaDTO } from 'app/core/model/both/FichaCentroEstadisticaDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { ReportesDTO } from 'app/core/model/both/ReportesDTO.model';
import { FormsModule } from '@angular/forms';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';

@Component({
  selector: 'app-registro-estadistico',
  standalone: true,
  imports: [NgApexchartsModule,
    CommonModule,
    MatIconModule,
    MatExpansionModule,
    MatSelectModule,
    FormsModule
  ],
  templateUrl: './registro-estadistico.component.html',
  styleUrl: './registro-estadistico.component.scss'
})
export class RegistroEstadisticoComponent implements OnInit, AfterViewInit {

  @ViewChild("chart") chart: any;
  public chartOptions: {
    series: ApexAxisChartSeries;
    chart: ApexChart;
    xaxis: ApexXAxis;
    yaxis: ApexYAxis;
    title: ApexTitleSubtitle;
    dataLabels: ApexDataLabels;
  };

  tiposDelito: CatalogoDTO[] = [];
  categories: any[] = []; // Para almacenar los nombres de los delitos
  seriesData: number[] = [];
  tipoDocumentoSeleccionado: string = '';

  @Input() mostrarPorNemonico: boolean = false;
  @Input() nemonico: string = '';

  graficoSeleccionado: string = '';

  centros: JerarquiaDTO[] = [];

  listaGraficos = [
    {
      nemonico: 'INFRACCION',
      nombre: 'Población por tipo de infracción',
    },
    {
      nemonico: 'EDAD',
      nombre: 'Población por rango de edad',
    },
    {
      nemonico: "CENTRO",
      nombre: 'Población total en centros juveniles',
    }
  ];

  tiposCentro = [
    {
      nemonico: "TODOS",
      nombre: 'Todos',
    },
    {
      nemonico: "SOA",
      nombre: 'SOA',
    },
    {
      nemonico: "CJDR",
      nombre: "CJDR",
    }
  ];

  titulo: string = '';
  tituloY: string = '';
  tituloPantalla: string = 'Registro estadístico';

  reportesExpedientesDTO: ReportesDTO;
  nemonicoCentro: string = 'TODOS';

  filtroCentro: string;
  filtroSexo: string | number = 1;

  listaTipoSexo: CatalogoDTO[] = [];

  constructor(private expedienteService: ExpedienteMatrizService,
    public funcionesUtils: FuncionesUtils,
    private fichaIndentificacionService: FichaIdentificacionService,
    private jerarquiaService: JerarquiaService,
    private cdr: ChangeDetectorRef
  ) {
    this.chartOptions = {
      series: [

      ],
      chart: {
        type: "line",
        height: 400,
        width: "100%"
      },
      title: {
        text: "",
        align: "center"
      },
      xaxis: {
        categories: [],
        labels: {
          show: true,
          rotate: -45, // Gira las etiquetas para que no se superpongan
          trim: true,
          style: {
            fontSize: "12px"
          }
        }
      },
      yaxis: {

      },
      dataLabels: {
        enabled: true
      }
    };
  }
  ngAfterViewInit(): void {
    if (this.mostrarPorNemonico) {
      this.mostrarGrafico(this.nemonico);
      this.cdr.detectChanges();
    }
  }
  ngOnInit(): void {

    if (this.mostrarPorNemonico) {
      this.mostrarGrafico(this.nemonico);
    }
    this.cargarDatosCatalogo();

  }

  actualizarGrafico() {
    this.chartOptions = {
      chart: {
        type: 'bar',
        height: 550,
        width: "100%",
      },
      xaxis: {
        categories: this.categories.length ? this.categories : ['Sin datos'],
        labels: {
          show: true,
          rotate: -45,
          trim: true,
          maxHeight: 100,
          style: {
            fontSize: "9px",
          }
        }
      },
      series: [
        {
          name: 'Cantidad',
          data: this.seriesData.length ? this.seriesData : [0]
        }
      ],
      title: {
        text: this.titulo,
        align: "center"
      },
      yaxis: {
        title: {
          text: this.tituloY
        }
      },
      dataLabels: {
        enabled: true
      }
    };

    setTimeout(() => {
      if (this.chart) {
        this.chart.updateOptions(this.chartOptions);
      }
    }, 500);

    this.cdr.detectChanges();
  }


  obtenerDatosInfraccion() {
    this.reportesExpedientesDTO = new ReportesDTO();
    console.log('this.nemonicoCentro', this.nemonicoCentro);
    this.reportesExpedientesDTO.nemonicoCentro = this.nemonicoCentro === 'TODOS' ? null : this.nemonicoCentro
    this.reportesExpedientesDTO.tokenIdentificadorCentro = this.filtroCentro
    this.reportesExpedientesDTO.nemonicoTipoSexo = this.filtroSexo !== 1 ? this.filtroSexo as string : null
    console.log('reportes', this.reportesExpedientesDTO);
    this.expedienteService.obtenerEstadisticasDelitosFiltros(etiquetasModel.NEMONICO_MENU_REPORTES_ESTADISTICOS, this.reportesExpedientesDTO).subscribe({
      next: (response: RespuestaPorDefecto<DelitoEstadisticaDTO[]>) => {
        this.categories = response.data.map(e =>
          e.nombreDelito
            ? e.nombreDelito.charAt(0).toUpperCase() + e.nombreDelito.slice(1).toLowerCase()
            : 'Desconocido' // Valor por defecto si es null o undefined
        );
        this.seriesData = response.data.map(e => e.cantidad);
        this.titulo = 'Población por tipo de infracción';
        this.tituloY = 'Cantidad de Delitos';
        // Llamar a la función que actualiza el gráfico
        this.chartOptions.chart = {
          type: 'bar',
          height: 600,
          width: "100%"
        }
        this.actualizarGrafico();
        this.chartOptions.series[0].name = "Cantidad de delitos";
        if (this.mostrarPorNemonico) {
          this.tituloPantalla = this.listaGraficos.find(e => e.nemonico == 'INFRACCION')?.nombre || '';
        }
      },
      error: (error) => console.error('Error obteniendo estadísticas:', error)
    });
  }

  verificarGraficoSeleccionado(event: string) {
    this.nemonicoCentro = 'TODOS'
    this.filtroSexo = 1;
    this.mostrarGrafico(event);
    this.graficoSeleccionado = event;
  }

  mostrarGrafico(nemonico: string) {
    if (nemonico == 'INFRACCION') {
      this.obtenerDatosInfraccion();
    } else if (nemonico == 'EDAD') {
      this.obtenerDatosEdad();
    }else if (nemonico == 'CENTRO') {
      this.obtenerDatosCentros();
    }
  }

  obtenerDatosEdad() {
    this.reportesExpedientesDTO = new ReportesDTO();
    console.log('this.nemonicoCentro', this.nemonicoCentro);
    this.reportesExpedientesDTO.nemonicoCentro = this.nemonicoCentro === 'TODOS' ? null : this.nemonicoCentro
    this.reportesExpedientesDTO.tokenIdentificadorCentro = this.filtroCentro
    this.reportesExpedientesDTO.nemonicoTipoSexo = this.filtroSexo !== 1 ? this.filtroSexo as string : null
    console.log('reportes', this.reportesExpedientesDTO);
    this.fichaIndentificacionService.obtenerEstadisticasEdades(etiquetasModel.NEMONICO_MENU_REPORTES_ESTADISTICOS, this.reportesExpedientesDTO).subscribe({
      next: (response: RespuestaPorDefecto<EdadEstadisticaDTO[]>) => {
        this.categories = response.data.map(e => e.edad);
        this.seriesData = response.data.map(e => e.cantidad);
        console.log('categorias', this.categories);
        console.log('series', this.seriesData);
        this.titulo = 'Población por rango de edad';
        this.tituloY = 'Cantidad de adolescentes';

        // Llamar a la función que actualiza el gráfico
        this.actualizarGrafico();
        this.chartOptions.chart = {
          type: 'bar',
          height: 600,
          width: "100%"
        }
        this.chartOptions.xaxis.labels.style.fontSize = "10px";
        this.chartOptions.series[0].name = "Cantidad de adolescentes";
        if (this.mostrarPorNemonico) {
          this.tituloPantalla = this.listaGraficos.find(e => e.nemonico == 'EDAD')?.nombre || '';
        }
      },
      error: (error) => console.error('Error obteniendo estadísticas:', error)
    });
  }

  obtenerDatosCentros() {
    this.reportesExpedientesDTO = new ReportesDTO();
    console.log('this.nemonicoCentro', this.nemonicoCentro);
    this.reportesExpedientesDTO.nemonicoCentro = this.nemonicoCentro === 'TODOS' ? null : this.nemonicoCentro
    this.reportesExpedientesDTO.tokenIdentificadorCentro = this.filtroCentro
    this.reportesExpedientesDTO.nemonicoTipoSexo = this.filtroSexo !== 1 ? this.filtroSexo as string : null
    console.log('reportes', this.reportesExpedientesDTO);
    this.jerarquiaService.obtenerEstadisticasFichasPorCentro(etiquetasModel.NEMONICO_MENU_REPORTES_ESTADISTICOS, this.reportesExpedientesDTO).subscribe({
      next: (response: RespuestaPorDefecto<FichaCentroEstadisticaDTO[]>) => {
        this.categories = response.data.map(e => e.nombreCentro);
        this.seriesData = response.data.map(e => e.cantidadFichas);
        console.log('categorias', this.categories);
        console.log('series', this.seriesData);
        this.titulo = 'Población por centros juveniles';
        this.tituloY = 'Cantidad de adolescentes';

        // Llamar a la función que actualiza el gráfico
        this.actualizarGrafico();
        this.chartOptions.chart = {
          type: 'bar',
          height: 450,
          width: "100%"
        }
        this.chartOptions.xaxis.labels.style.fontSize = "10px";
        this.chartOptions.series[0].name = "Cantidad de adolescentes";
      },
      error: (error) => console.error('Error obteniendo estadísticas:', error)
    });
  }

  verificarCentro(event: string) {
    // this.mostrarGrafico(event);
    // this.graficoSeleccionado = event;
    // this.obtenerDatosCentros(event);
    this.filtroCentro = null;
    this.mostrarGrafico(this.graficoSeleccionado);
    if (this.nemonicoCentro == 'SOA' || this.nemonicoCentro == 'CJDR') {
      this.getCentros();
    }
  }

  onChangeCentro(nuevoCentroId: string): void {
    this.filtroCentro = nuevoCentroId;
    this.mostrarGrafico(this.graficoSeleccionado);
  }

  getCentros(): void {
    this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre(this.nemonicoCentro, '').subscribe({
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

  cargarDatosCatalogo() {
    this.funcionesUtils
      .obtenerListaCatalogo('TIPO_SEXO', '')
      .subscribe({
        next: (data) => (this.listaTipoSexo = data),
        error: (error) =>
          console.error('Error cargando tipos de sexo:', error),
      });
  }

  seleccionSexo(event: string) {
    this.filtroSexo = event;
    this.mostrarGrafico(this.graficoSeleccionado);
  }

}
