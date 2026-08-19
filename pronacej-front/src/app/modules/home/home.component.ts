import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { AlertaService } from '../general/services/alerta.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionarioService } from '../seguridad/services/funcionario.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { AlertaDTO } from 'app/core/model/both/AlertaDTO.model';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { ApexChart, ApexDataLabels, ApexLegend, ApexNonAxisChartSeries, ApexOptions, ApexPlotOptions, ApexResponsive, ApexTitleSubtitle, NgApexchartsModule } from 'ng-apexcharts';
import { FichaIdentificacionService } from '../administracion/services/fichaIdentificacion.service';
import { EstadoAdolescenteEstadisticoDTO } from 'app/core/model/both/EstadoAdolescenteEstadisticoDTO.model';
import { MatTableModule } from '@angular/material/table';
import { TrabajoLaboralService } from '../seguridad/services/trabajoLaboral.service';
import { TrabajoLaboralEstadisticoDTO } from 'app/core/model/both/TrabajoLaboralEstadisticoDTO.model';
import { EstudiosService } from '../seguridad/services/EstudiosService.service';
import { EstudiosEstadisticoDTO } from 'app/core/model/EstudiosEstadisticoDTO.model';
import { AuthSerguridadServicio } from '../seguridad/services/auth.seguridad.service';
import { MenuDTO } from 'app/core/model/both/seguridad/MenuDTO.model';

export type ChartOptions = {
  series: any;
  chart: ApexChart;
  labels: string[];
  responsive: ApexResponsive[];
  title: ApexTitleSubtitle;
  legend: ApexLegend;
  dataLabels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  xaxis?: any;
  yaxis?: any;
  colors?: string[];
  tooltip?: any;
};

interface ListadoItem {
  nombre: string;
  cantidad: number;
  color: string;
  icono: string;
  ruta: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatListModule,
    MatExpansionModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    NgApexchartsModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;
  titulo: string = "Bienvenido al Sistema de Gestión Integral de Gestión del Adolescente Infractor";
  noticias: any[] = [];

  alertas: AlertaDTO[] = [];
  cantidadAlertas: number = 0;
  funcionarioActivo: FuncionarioDTO = new FuncionarioDTO();

  currentSlide = 0;
  slides: NodeListOf<Element> = document.querySelectorAll('.carousel .slide');
  cantidadTrabajoActivo: number = 0;
  estadisticasTrabajoLaboral: TrabajoLaboralEstadisticoDTO[] = [];
  displayedColumnsTrabajoLaboral: string[] = ['institucion', 'ruc', 'cantidad'];

  cantidadUsuariosEstudiando: number = 0;
porcentajeConvenioPronacej: number = 0;

estadisticasEstudios: EstudiosEstadisticoDTO[] = [];

displayedColumnsEstudios: string[] = ['institucion', 'ruc', 'cantidad'];

public chartOptionsEstudios: Partial<ChartOptions> = {
  series: [],
  labels: [],
  chart: {
    type: 'donut',
    height: 400
  },
  title: {
    text: 'Estudios por institución',
    align: 'center'
  },
  legend: {
    position: 'bottom'
  },
  dataLabels: {
    enabled: true,
    formatter: (val: any) => `${val.toFixed(1)}%`
  }
};

  // Inicializar chartOptions con valores por defecto
  public chartOptions: Partial<ChartOptions> = {
    series: [],
    labels: [],
    chart: {
      type: 'radialBar',
      height: 400
    },
    title: {
      text: "Estados de Adolescentes",
      align: 'center'
    },
    legend: {
      position: 'bottom'
    }
  };

  // Inicializar chartOptionsTipoSexo con valores por defecto
  public chartOptionsTipoSexo: Partial<ChartOptions> = {
    series: [],
    labels: [],
    chart: {
      type: 'donut',
      height: 400
    },
    title: {
      text: "Estados de Adolescentes",
      align: 'center'
    },
    legend: {
      position: 'bottom'
    },
    dataLabels: {
      enabled: true,
      formatter: (val: any) => `${val.toFixed(1)}%`
    }
  };

  listado = [    
    { nombre: 'Expediente matriz', color: 'skyblue', icono: 'account_box', ruta: 'gestion-adolescente/ficha-identificacion' },
    { nombre: 'Registro salida', color: 'skyblue', icono: 'assignment', ruta: 'salida/registro-salida' },
  ];

  /////////////////////////////
  // TABLA DE RIESGOS 
  /////////////////////////////
  displayedColumnsAlertas: string[] = ['prioridad', 'mensaje', 'adolescente', 'link'];

  // Contadores de alertas por prioridad
  totalAlertas = 0;
  resumenAlertas = '';

  alertasPorPrioridad = {
    ALTA: { count: 0, percentage: 0, color: '#D32F2F' }, // Rojo
    MEDIA: { count: 0, percentage: 0, color: '#F57C00' }, // Naranja
    BAJA: { count: 0, percentage: 0, color: '#FBC02D' } // Amarillo
  };

  // Inicializar chartSeriesAlertas con array vacío
  chartSeriesAlertas: number[] = [];
  
  // Inicializar chartOptionsAlertas con valores por defecto
  chartOptionsAlertas: ApexOptions = {
    chart: {
      type: 'radialBar',
      height: 350
    },
    plotOptions: {
      radialBar: {
        hollow: {
          size: '60%'
        },
        dataLabels: {
          name: {
            show: false
          },
          value: {
            fontSize: '24px',
            offsetY: -10
          },
          total: {
            show: true,
            label: '0 ALERTAS PENDIENTES',
            color: '#333',
            formatter: () => '0'
          }
        },
        track: {
          show: true
        }
      }
    },
    colors: ['#D32F2F', '#F57C00', '#FBC02D'],
    labels: ['Alta', 'Media', 'Baja'],
    tooltip: {
      enabled: true,
      theme: 'dark'
    },
    series: []
  };

  public chartOptionsTrabajoLaboral: Partial<ChartOptions> = {
  series: [],
  labels: [],
  chart: {
    type: 'bar',
    height: 400,
    toolbar: {
      show: false
    }
  },
  plotOptions: {
    bar: {
      horizontal: true,
      distributed: true,
      borderRadius: 8,
      barHeight: '55%'
    }
  },
  title: {
    text: 'Trabajo laboral por institución',
    align: 'center'
  },
  legend: {
    show: false
  },
  dataLabels: {
    enabled: true
  } as any
};


  constructor(
    private alertaService: AlertaService,
    private dialogMensajeService: DialogMensajeService,
    private funcionarioService: FuncionarioService,
    private router: Router,
    private authSeguridadService: AuthSerguridadServicio,
    private fichaIdentificacionService: FichaIdentificacionService,
    private trabajoLaboralService: TrabajoLaboralService,
    private estudiosService: EstudiosService) { }

  ngOnInit(): void {
    this.authSeguridadService.verificarPermisoPantalla('MENU_DASHBOARD_ESTADISTICAS_ADOL').subscribe({
      next: (response: RespuestaPorDefecto<MenuDTO>) => {
        if (!response.sinAcceso) {
          this.listado.push({ nombre: 'Dashboard\nestadístico', color: 'skyblue', icono: 'analytics', ruta: 'dashboard' });
        }
      }
    });

    this.obtenerFuncionario();
    this.cargarNoticias();
    this.obtenerDatosEdad();
    this.obtenerDatosSexo();
    this.obtenerIndicadoresTrabajoLaboral();
    this.obtenerIndicadoresEstudios();
  }

  ngAfterViewInit() {
    // Ejecuta el código después de que la vista se haya inicializado
    this.slides = document.querySelectorAll('.carousel .slide');

    if (this.slides.length > 0) {
      this.showSlide(this.currentSlide);

      // Configura el auto desplazamiento del carrusel
      setInterval(() => {
        this.moveSlide(1);
      }, 25000);
    }
  }

  showSlide(index: number) {
    if (this.slides.length === 0) return;
    
    if (index >= this.slides.length) {
      this.currentSlide = 0;
    } else if (index < 0) {
      this.currentSlide = this.slides.length - 1;
    } else {
      this.currentSlide = index;
    }
    const carousel = document.querySelector('.carousel') as HTMLElement;
    if (carousel) {
      carousel.style.transform = `translateX(-${this.currentSlide * 100}%)`;
    }
  }

  moveSlide(n: number) {
    this.showSlide(this.currentSlide + n);
  }

  obtenerFuncionario() {
    this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          if (!response.exito) {
            return;
          }

          this.funcionarioActivo = response.data;
          this.obtenerAlertas();
        },
        error: (error: any) => {
          console.log('Hubo un problema al recuperar los registros. Inténtalo de nuevo.');
        }
      }
    );
  }

  redirigir(alerta: AlertaDTO) {
    //this.router.navigate(["gestion-adolescente/ficha-identificacion/crear-editar/fichaPrincipal/" + alerta.tokenFichaIdentificacion]);
    this.router.navigate([(alerta.ruta ? alerta.ruta : "gestion-adolescente/ficha-identificacion/crear-editar/fichaPrincipal/") + alerta.tokenFichaIdentificacion]);
  }

  obtenerAlertas() {
    let alertaDTO = new AlertaDTO();
    alertaDTO.tokenCentro = this.funcionarioActivo.tokenIdentificadorDepartamento;

    this.alertaService.obtenerAlertas(alertaDTO, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<AlertaDTO[]>) => {
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            return;
          }

          this.alertas = response.data || [];

          // Ordenar las alertas por prioridad (alta-media-baja)
          this.alertas.sort((a, b) => {
            const prioridadOrden = { 'alta': 1, 'media': 2, 'baja': 3 };
            return prioridadOrden[a.prioridad] - prioridadOrden[b.prioridad];
          });

          this.procesarAlertas();
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
        }
      }
    );
  }

  esCritica(fechaLimite: Date): boolean {
    const hoy = new Date();
    const fecha = new Date(fechaLimite);
    const diferenciaDias = (fecha.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24);
    return diferenciaDias <= 7;
  }

  calcularDiasFaltantes(fechaLimite: Date): number {
    const hoy = new Date();
    const fecha = new Date(fechaLimite);
    return Math.ceil((fecha.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
  }

  cargarNoticias(): void {
    // Datos de prueba
    this.noticias = [
      {
        titulo: 'Noticia 1',
        resumen: 'Este es el resumen de la noticia 1. Se trata de un evento importante.'
      },
      {
        titulo: 'Noticia 2',
        resumen: 'Resumen de la noticia 2. Aquí se mencionan los detalles más relevantes.'
      },
      {
        titulo: 'Noticia 3',
        resumen: 'La noticia 3 trata sobre otro evento de gran interés.'
      }
    ];
  }

  formatFecha(fecha: any): string {
    // Si fecha no es una instancia de Date, convertirla
    const date = new Date(fecha);
    if (isNaN(date.getTime())) {
      return ''; // Retorna vacío si la fecha no es válida
    }

    const day = date.getDate().toString().padStart(2, '0'); // Asegura que el día tenga 2 dígitos
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Meses en JavaScript son 0-indexados
    const year = date.getFullYear();

    return `${day}-${month}-${year}`;
  }

  obtenerDatosEdad() {
    this.fichaIdentificacionService.obtenerEstadisticasEstados(etiquetasModel.NEMONICO_MENU_REPORTES_ESTADISTICOS).subscribe({
      next: (response: RespuestaPorDefecto<EstadoAdolescenteEstadisticoDTO[]>) => {
        if (response.exito && response.data) {
          const data = response.data;

          // Separar nombres de estados y sus cantidades
          const nombresEstados = data.map(item => item.nombreEstado);
          const cantidades = data.map(item => item.cantidad);

          // Configuración del gráfico
          this.chartOptions = {
            series: cantidades, // Asegúrate de que este array tiene los números correctos
            labels: nombresEstados, // Ejemplo: ['Serious Risks', 'Moderate Risk', 'Minor Risk', 'Advice']
            chart: {
              type: 'radialBar',
              height: 400
            },
            title: {
              text: "Estados de Adolescentes",
              align: 'center'
            },
            legend: {
              position: 'bottom'
            },
            plotOptions: {
              radialBar: {
                dataLabels: {
                  name: {
                    show: true,
                  },
                  value: {
                    show: true,
                  },
                  total: {
                    show: true,
                    label: 'Total adolescentes',
                    formatter: () => {
                      // Suma todas las cantidades y muestra en el centro
                      return `${cantidades.reduce((a, b) => a + b, 0)}`;
                    }
                  }
                },
                hollow: {
                  size: '70%',
                },
                track: {
                  background: '#e7e7e7',
                  strokeWidth: '100%'
                }
              }
            }
          };
        }
      },
      error: (error) => console.error('Error obteniendo estadísticas:', error)
    });
  }

  obtenerDatosSexo() {
    this.fichaIdentificacionService.obtenerEstadisticasSexo(etiquetasModel.NEMONICO_MENU_REPORTES_ESTADISTICOS).subscribe({
      next: (response: RespuestaPorDefecto<EstadoAdolescenteEstadisticoDTO[]>) => {
        if (response.exito && response.data) {
          const data = response.data;

          // Separar nombres de estados y sus cantidades
          const nombresEstados = data.map(item => item.nombreEstado);
          const cantidades = data.map(item => item.cantidad);

          // Configuración del gráfico
          this.chartOptionsTipoSexo = {
            series: cantidades,
            labels: nombresEstados,
            chart: {
              type: 'donut',
              height: 400
            },
            title: {
              text: "Estados de Adolescentes",
              align: 'center'
            },
            legend: {
              position: 'bottom'
            },
            dataLabels: {
              enabled: true,
              formatter: (val: any) => `${val.toFixed(1)}%`
            }
          };
        }
      },
      error: (error) => console.error('Error obteniendo estadísticas:', error)
    });
  }

  irA(url: string) {
    this.router.navigate([url]);
  }

  procesarAlertas() {
    // Reiniciar contadores
    this.alertasPorPrioridad.ALTA.count = 0;
    this.alertasPorPrioridad.MEDIA.count = 0;
    this.alertasPorPrioridad.BAJA.count = 0;

    // Contar alertas por prioridad
    this.alertas.forEach(alerta => {
      const prioridadKey = alerta.prioridad.toUpperCase() as 'ALTA' | 'MEDIA' | 'BAJA';
      if (this.alertasPorPrioridad[prioridadKey]) {
        this.alertasPorPrioridad[prioridadKey].count++;
      }
    });

    // Total de alertas
    this.totalAlertas = this.alertas.length;

    // Calcular porcentajes con máximo 2 decimales
    Object.keys(this.alertasPorPrioridad).forEach(key => {
      const prioridad = key as 'ALTA' | 'MEDIA' | 'BAJA';
      this.alertasPorPrioridad[prioridad].percentage =
        this.totalAlertas > 0
          ? Number(((this.alertasPorPrioridad[prioridad].count / this.totalAlertas) * 100).toFixed(2))
          : 0;
    });

    // Resumen en texto
    this.resumenAlertas = `${this.alertasPorPrioridad.ALTA.count} Alta(s) / ` +
      `${this.alertasPorPrioridad.MEDIA.count} Media(s) / ` +
      `${this.alertasPorPrioridad.BAJA.count} Baja(s)`;

    // Series de datos para el gráfico
    this.chartSeriesAlertas = [
      this.alertasPorPrioridad.ALTA.percentage,
      this.alertasPorPrioridad.MEDIA.percentage,
      this.alertasPorPrioridad.BAJA.percentage
    ];

    // Opciones del gráfico
    this.chartOptionsAlertas = {
      chart: {
        type: 'radialBar',
        height: 350
      },
      plotOptions: {
        radialBar: {
          hollow: {
            size: '60%'
          },
          dataLabels: {
            name: {
              show: false
            },
            value: {
              fontSize: '24px',
              offsetY: -10
            },
            total: {
              show: true,
              label: `${this.totalAlertas} ALERTAS PENDIENTES`,
              color: '#333',
              formatter: () => `${this.totalAlertas}`
            }
          },
          track: {
            show: true
          }
        }
      },
      colors: [
        this.alertasPorPrioridad.ALTA.color,
        this.alertasPorPrioridad.MEDIA.color,
        this.alertasPorPrioridad.BAJA.color
      ],
      labels: ['Alta', 'Media', 'Baja'],
      tooltip: {
        enabled: true,
        theme: 'dark'
      },
      series: this.chartSeriesAlertas
    };
  }

  getNombreCompleto(alerta: AlertaDTO): string {
    return `${alerta.apellidoPaternoAdolescente || ''} ${alerta.apellidoMaternoAdolescente || ''} ${alerta.nombresAdolescente || ''}`.trim();
  }

  getColor(prioridad: String) {
    switch (prioridad) {
      case "alta":
        return '🔴';
      case "media":
        return '🟠';
      case "baja":
        return '🟡';
      default:
        return '⚪';
    }
  }

  obtenerIndicadoresTrabajoLaboral(): void {
  this.trabajoLaboralService.obtenerCantidadTrabajoActivo(
    etiquetasModel.NEMONICO_MENU_TRABAJO_LABORAL
  ).subscribe({
    next: (response: RespuestaPorDefecto<number>) => {
      if (response.exito) {
        this.cantidadTrabajoActivo = response.data || 0;
      }
    },
    error: (error) => {
      console.error('Error obteniendo cantidad de trabajo activo:', error);
    }
  });

  this.trabajoLaboralService.obtenerEstadisticasTrabajoLaboral(
    etiquetasModel.NEMONICO_MENU_TRABAJO_LABORAL
  ).subscribe({
    next: (response: RespuestaPorDefecto<TrabajoLaboralEstadisticoDTO[]>) => {
      if (response.exito) {
        this.estadisticasTrabajoLaboral = response.data || [];

        const labels = this.estadisticasTrabajoLaboral.map(
          item => item.nombreInstitucion || 'Sin institución'
        );

        const cantidades = this.estadisticasTrabajoLaboral.map(
          item => item.cantidad || 0
        );

        this.chartOptionsTrabajoLaboral = {
          series: [
            {
              name: 'Cantidad',
              data: cantidades
            }
          ],
          chart: {
            type: 'bar',
            height: 400,
            toolbar: {
              show: false
            }
          },
          plotOptions: {
            bar: {
              horizontal: false,
              columnWidth: '45%',
              borderRadius: 8
            }
          },
          dataLabels: {
            enabled: true,
            formatter: (valor: number) => `${Math.round(valor)}`
          } as any,
          xaxis: {
            categories: labels,
            labels: {
              rotate: -35,
              trim: true,
              style: {
                fontSize: '11px'
              }
            }
          },
          yaxis: {
            forceNiceScale: true,
            decimalsInFloat: 0
          },
          colors: ['#2196f3'],
          title: {
            text: 'Trabajo laboral por institución',
            align: 'center'
          },
          tooltip: {
            enabled: true,
            theme: 'dark'
          }
        };
      }
    },
    error: (error) => {
      console.error('Error obteniendo estadísticas de trabajo laboral:', error);
    }
  });
}

  obtenerIndicadoresEstudios(): void {
  this.estudiosService.obtenerCantidadUsuariosEstudiando(
    etiquetasModel.NEMONICO_MENU_INICIO
  ).subscribe({
    next: (response: RespuestaPorDefecto<number>) => {
      if (response.exito) {
        this.cantidadUsuariosEstudiando = response.data || 0;
      }
    },
    error: (error) => {
      console.error('Error obteniendo cantidad de usuarios estudiando:', error);
    }
  });

  this.estudiosService.obtenerPorcentajeConvenioPronacej(
    etiquetasModel.NEMONICO_MENU_INICIO
  ).subscribe({
    next: (response: RespuestaPorDefecto<number>) => {
      if (response.exito) {
        this.porcentajeConvenioPronacej = response.data || 0;
      }
    },
    error: (error) => {
      console.error('Error obteniendo porcentaje de convenio PRONACEJ:', error);
    }
  });

  this.estudiosService.obtenerEstadisticasEstudios(
    etiquetasModel.NEMONICO_MENU_INICIO
  ).subscribe({
    next: (response: RespuestaPorDefecto<EstudiosEstadisticoDTO[]>) => {
      if (response.exito) {
        this.estadisticasEstudios = response.data || [];

        const labels = this.estadisticasEstudios.map(
          item => item.nombreInstitucion || 'Sin institución'
        );

        const cantidades = this.estadisticasEstudios.map(
          item => item.cantidad || 0
        );

        this.chartOptionsEstudios = {
          series: [
            {
              name: 'Cantidad',
              data: cantidades
            }
          ],
          chart: {
            type: 'bar',
            height: 400,
            toolbar: {
              show: false
            }
          },
          plotOptions: {
            bar: {
              horizontal: false,
              columnWidth: '45%',
              borderRadius: 8
            }
          },
          dataLabels: {
            enabled: true,
            formatter: (valor: number) => `${Math.round(valor)}`
          } as any,
          xaxis: {
            categories: labels,
            labels: {
              rotate: -35,
              trim: true,
              style: {
                fontSize: '11px'
              }
            }
          },
          yaxis: {
            forceNiceScale: true,
            decimalsInFloat: 0
          },
          colors: ['#2196f3'],
          title: {
            text: 'Estudios por institución',
            align: 'center'
          },
          tooltip: {
            enabled: true,
            theme: 'dark'
          }
        };
      }
    },
    error: (error) => {
      console.error('Error obteniendo estadísticas de estudios:', error);
    }
  });
}

}