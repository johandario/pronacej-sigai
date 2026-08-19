import { CommonModule, Location } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { RegistroSalidaDTO } from 'app/core/model/both/salida/RegistroSalidaDTO.model';
import { TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { GestionFugaService } from 'app/modules/flujo-trabajo/gestion-fuga/gestion-fuga.service';
import { TrasladoService } from 'app/modules/flujo-trabajo/traslado/traslado.service';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { catchError, concatMap, iif, map, Observable, of, tap, throwError } from 'rxjs';
import { MatStepperModule } from '@angular/material/stepper';
import { MatStepper } from '@angular/material/stepper';
import { HttpClient } from '@angular/common/http';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';

@Component({
  selector: 'app-vista-salida-traslados',
  standalone: true,
  imports: [FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatIconModule,
    MatCardModule,
    MatAutocompleteModule,
    CommonModule,
    MatStepperModule],
  templateUrl: './vista-salida-traslados.component.html',
  styleUrl: './vista-salida-traslados.component.scss'
})
export class VistaSalidaTrasladosComponent implements OnInit {
  

  tokenID: string;
  traslado: TrasladoDTO;
  salidaCargada: boolean = false;
  base64Image: string | null = null;

  registroSalida: RegistroSalidaDTO = new RegistroSalidaDTO();

  fuga: GestionFugaDTO;
  fechaFuga: string;
  fechaDirector: string;
  fechaApoderado: string;
  catalogosParentezco: CatalogoDTO[] = [];
  nemonicoParentezco: string = "PARENTESCO";
  nombreParentesco: string;
  indiceSeleccionado = 0;
  esTrasladoTemporal: boolean = false;
  nemonicoMenu = etiquetasModel.NEMONICO_FLUJO_BORRADORES_TRASLADOS;

  @ViewChild(MatStepper) stepper: MatStepper; 

  constructor(
    private fb: FormBuilder,
    private trasladoService: TrasladoService,
    private dialogMensajeService: DialogMensajeService,
    private route: ActivatedRoute,
    private router: Router,
    private _location: Location,
    private pdfService: PdfService,
    public funcionesUtils: FuncionesUtils,
    private jerarquiaService: JerarquiaService,
    private fichaIdentificacionService: FichaIdentificacionService,
    private catalogoService: CatalogoService,
    private funcionarioService: FuncionarioService,
    private fugaService: GestionFugaService,
    private http: HttpClient, 
  ) { }
  

  async ngOnInit(): Promise<void> {

    const registroSalida: RegistroSalidaDTO = history.state.traslado;
    if (registroSalida) {
      this.registroSalida = registroSalida;
    }
    await this.getCatalogosHijos();

    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');
    this.obtenerParametrosDeConsulta().pipe(
      concatMap(() =>
        iif(
          () => this.tokenID ? true : false,
          registroSalida.traslado? this.obtenerTraslado() : this.obtenerFuga(),
          of(null),
        )
      )
    ).subscribe({
      next: () => {
        load.close();
      },
      error: (err) => {
        console.error('Error durante la ejecución:', err);
        load.close();
      },
      complete: () => load.close(),
    });


  }

  irAlPaso(paso: number) {
    this.stepper.selectedIndex = paso;
  }

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tokenID = this.route.snapshot.params['tokenID'];
        if (tokenID) {
          this.tokenID = tokenID;
        }
      })
    );
  }

  obtenerTraslado(): Observable<any> {
    return this.trasladoService.obtenerTrasladoPorTokenID(this.tokenID,this.nemonicoMenu).pipe(
      tap((response) => {
        this.traslado = response.data;
        console.log(this.traslado);
        this.esTrasladoTemporal = this.traslado?.instanciaProcesoDTO?.proceso?.nemonico === 'TRASLADO_TEMPORAL';
        
        this.salidaCargada = true;
      }),
      catchError(err => {
        this.trasladoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerFuga(): Observable<any> {
    return this.fugaService.obtenerFugasPorTokenID(this.tokenID, this.nemonicoMenu).pipe(
      tap((response) => {
        this.fuga = response.data;
        console.log(this.fuga);
        
        const fecha = new Date(this.fuga.fechaFuga);
        this.fechaFuga = fecha.toISOString().split('T')[0];
        const fechaDirector = new Date(this.fuga.fechaInformeDirector);
        this.fechaDirector= fechaDirector.toISOString().split('T')[0];
        const fechaApoderado = new Date(this.fuga.fechaInformeApoderado);
        this.fechaApoderado= fechaApoderado.toISOString().split('T')[0];
        const parentescoSeleccionado = this.catalogosParentezco.find(
          (catalogo) => catalogo.idCatalogo === this.fuga.parentesco.idCatalogo
        );
        this.nombreParentesco = parentescoSeleccionado ? parentescoSeleccionado.nombre : '';
        this.salidaCargada = true;
      }),
      catchError(err => {
        this.fugaService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  cancelar() {
    this._location.back();
  }

  pruebaPdf() {
    // Object.assign(this.traslado, this.informeFormGroup.value);

    let listaAdolescentes: string = '';

    for (let adolescente of this.traslado.trasladoAdolescentes) {
      listaAdolescentes += `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}, `;
    }

    let request = new GeneracionPdfRequest();
    request.nemonico = etiquetasModel.FORMULARIO_TRASLADO;
    request.variables = {
      // "[TITULO-INFORME]": this.tareaEntrante.paso.nombre,
      "[CENTRO-ORIGEN]": this.traslado.centroOrigen.nombre,
      "[CENTRO-DESTINO]": this.traslado.centroDestino.nombre,
      "[TIPO-TRASLADO]": this.traslado.motivoTraslado.nombre,
      "[LISTA-ADOLESCENTES]": listaAdolescentes,
      "[ANTECEDENTES]": this.traslado.antecedentes,
      "[ANALISIS]": this.traslado.analisis,
      "[CONCLUSIONES]": this.traslado.conclusiones,
      "[RECOMENDACIONES]": this.traslado.recomendaciones
    }
    this.pdfService.generarPdf(request, '').subscribe({
      next: (response: RespuestaPorDefecto<string>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        console.log(response);

        const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

        const pwa = window.open(url);

        // // Crear un enlace y disparar la descarga
        // const a = document.createElement('a');
        // a.href = url;
        // a.download = 'archivo.pdf'; // Nombre del archivo
        // document.body.appendChild(a);
        // a.click();

        // // Limpiar la URL y remover el enlace
        // window.URL.revokeObjectURL(url);
        // document.body.removeChild(a);
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  getCatalogosHijos(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.catalogoService.obtenerHijos('PARENTESCO', '').subscribe({
        next: (responseCatalog) => {
          this.catalogosParentezco = responseCatalog.data;
          resolve();
        },
        error: (error) => {
          console.error('Error al obtener catálogos:', error);
          reject(error);
        },
      });
    });
  }

  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatHora(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }

   loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  generarPdfFuga(){
     this.loadImageAsBase64();
         setTimeout(() => {
         const fechaRegistro = this.formatFecha((new Date).toString())
         const horaRegistro= this.formatHora((new Date).toString())
         const titulopantala= "Informe de fuga"
         let request = new GeneracionPdfRequest();
         request.nemonico = etiquetasModel.FORMULARIO_FUGA_APODERADO;
         request.variables = {
           "[IMG_BASE64]": this.base64Image,
           "[TITULO-PLANTILLA]": titulopantala,
           "[TITULO-INFORME]": titulopantala,
           "[FECHA_REGISTRO]": fechaRegistro,
           "[HORA_REGISTRO]": horaRegistro,
           "[DESCRIPCION]": this.fuga.descripcionHechos,
           "[ACCIONES-REALIZADAS]": this.fuga.accionesRealizadas,
           "[APODERADO]": this.fuga.apoderado,
           "[DNI]": this.fuga.dni,
           "[FECHA-APODERADO]": this.fuga.fechaInformeApoderado? new Date(this.fuga.fechaInformeApoderado).toISOString().split('T')[0]: "Fecha no disponible",
           "[NUMERO-IDENTIFICACION]": this.fuga.numeroIdentificacion,
           "[NOMBRE-ADOLESCENTE]": this.fuga.nombreAdolescente,
           "[EDAD]": `${this.funcionesUtils.getEdad(String(this.fuga.fechaNacimiento))}`,
          //  "[CENTRO]": this.funcionarioActivo.departamento,
         }
         this.pdfService.generarPdf(request, '').subscribe({
           next: (response: RespuestaPorDefecto<string>) => {
             if (!response.exito) {
               this.dialogMensajeService.mensajeError(
                 'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
               );
               return;
             }
             const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
         const pwa = window.open(url);
           },
           error: (error: any) => {
             this.dialogMensajeService.mensajeError(
               'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
             );
           }
         });
       }, 500); 
  }
  
  
    generarPdfTraslado(){
       this.loadImageAsBase64();
         setTimeout(() => {
       let listaAdolescentes: string = '';
          for (let adolescente of this.traslado.trasladoAdolescentes) {
            listaAdolescentes += `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}, `;
          }
          let tablaAdolescentes = new TablaPlantilla();
          tablaAdolescentes.encabezados = [
            'Nombre', 'DNI'
          ];
          tablaAdolescentes.filas = this.traslado.trasladoAdolescentes.map(adolescente => {
            console.log(adolescente);
            return {
              'Nombre': `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}`,
              'DNI': adolescente.fichaIdentificacion.numeroIdentificacion ?? ""
            };
          });
          const fechaActual = new Date();
          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_TRASLADO;
          request.variables = {
            "[TITULO-PLANTILLA]": "Informe de traslado",
            "[IMG_BASE64]": this.base64Image,
            "[TITULO-INFORME]": "Informe de traslado",
            "[FECHA]": this.funcionesUtils.formatearFecha(fechaActual),
            "[HORA]": this.funcionesUtils.formatearHora(fechaActual),
            "[CENTRO]": this.traslado.centroOrigen.nombre,
            "[CENTRO-ORIGEN]": this.traslado.centroOrigen.nombre,
            "[CENTRO-DESTINO]": this.traslado.centroDestino.nombre,
            "[TIPO-TRASLADO]": this.traslado.motivoTraslado.nombre,
            "[TABLA-ADOLESCENTES]": JSON.stringify(tablaAdolescentes),
            "[ANTECEDENTES]": this.traslado.antecedentes,
            "[ANALISIS]": this.traslado.analisis,
            "[CONCLUSIONES]": this.traslado.conclusiones,
            "[RECOMENDACIONES]": this.traslado.recomendaciones,
            "[SOLICITUD]": this.traslado.descripcionSolicitud,
            "[RECHAZO]": this.traslado.comentarioRechazo
          }
          this.pdfService.generarPdf(request, '').subscribe({
            next: (response: RespuestaPorDefecto<string>) => {
      
              if (!response.exito) {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                );
                return;
              }
              console.log(response);
              const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));
              const pwa = window.open(url);
            },
            error: (error: any) => {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
              );
            }
          });
            }, 500); 
    }
}
