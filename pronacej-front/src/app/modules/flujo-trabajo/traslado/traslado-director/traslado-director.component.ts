import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TrasladoService } from '../traslado.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { ActivatedRoute, Router } from '@angular/router';
import { TrasladoDTO } from 'app/core/model/both/tras/TrasladoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { Location } from '@angular/common'
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { TareaDTO, TareaTrasladoDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { FlujoTrabajoService } from '../../flujo-trabajo.service';
import { MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { catchError, concatMap, iif, Observable, of, tap, throwError } from 'rxjs';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { PdfService } from 'app/core/services/pdf.service';

@Component({
  selector: 'app-traslado-director',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatStepperModule,
    MatIconModule,
  ],
  templateUrl: './traslado-director.component.html',
  styleUrl: './traslado-director.component.scss'
})
export class TrasladoDirectorComponent implements OnInit {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INICIO;

  tokenID: string;
  estado: string = '';
  traslado: TrasladoDTO = new TrasladoDTO();
  trasladoTemporal: TrasladoDTO;
  centros: JerarquiaDTO[];
  tareaEntrante: TareaDTO = new TareaDTO;
  tareaSaliente: TareaDTO = new TareaDTO;
  listaTareas: TareaDTO[];

  trasladoCargado: boolean = false;

  datosCargados: boolean = false;

  tareasCargadas: boolean = false;

  funcionarioActivo: FuncionarioDTO;

  informeFormGroup = this.fb.group({
    descripcionSolicitud: ['', Validators.required],
    centroDestino: [null],
    conclusiones: ['', Validators.required],
    recomendaciones: ['', Validators.required],
  })

  constructor(
    private fb: FormBuilder,
    private trasladoService: TrasladoService,
    private dialogMensajeService: DialogMensajeService,
    private route: ActivatedRoute,
    private router: Router,
    private _location: Location,
    private pdfService: PdfService,
    private jerarquiaService: JerarquiaService,
    private flujoTrabajoService: FlujoTrabajoService,
    public funcionesUtils: FuncionesUtils,
    public funcionarioService: FuncionarioService,
  ) { }

  ngOnInit(): void {
    //obtengo tarea entrante del step seleccionado y lista de tareas del flujo
    if (history.state.tareaEntrante && history.state.listaTareas) {
      this.tareaEntrante = history.state.tareaEntrante;
      this.listaTareas = history.state.listaTareas;
      //asignar tareaEntrante en base a la lista para validar la vista
      //asignar tareaSaliente para enviar en la petición de guardado de traslado
    }

    this.cargarDatos();
  }

  cargarDatos(): void {
    //Existe el parámetro de URL tokenID?
    // Si: Cargo traslado
    // No: tengo el formulario para poder llenar
    const load = this.dialogMensajeService.mensajeLoading('Cargando datos...');

    this.obtenerParametrosDeConsulta().pipe(
      concatMap(() => this.obtenerFuncionario()),
      concatMap(() =>
        iif(
          () => this.tokenID ? true : false,
          this.obtenerTraslado(),
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

  cancelar() {
    // this.router.navigate([`/flujo-trabajo/bandeja-entrada`]);
    this._location.back();
  }

  obtenerParametrosDeConsulta(): Observable<any> {
    return this.route.queryParams.pipe(
      tap((params) => {
        const tokenTarea = params['tokenTarea'];
        if (tokenTarea) {
          this.tareaSaliente = this.listaTareas.find(tarea => tarea.tokenIdentificador === tokenTarea);
        }
        const tokenID = this.route.snapshot.params['tokenID'];
        if (tokenID) {
          this.tokenID = tokenID;
        }
      })
    );
  }

  obtenerFuncionario(): Observable<any> {
    return this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).pipe(
      tap((response) => {
        this.funcionarioActivo = response.data;
        this.datosCargados = true;
      }),
      catchError(err => {
        this.funcionarioService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  obtenerTraslado(): Observable<any> {
    return this.trasladoService.obtenerTrasladoPorTokenID(this.tokenID, this.nemonicoMenu).pipe(
      tap((response) => {
        this.informeFormGroup.patchValue(response.data);
        this.traslado = response.data;
        console.log(this.traslado);
        this.trasladoCargado = true;
      }),
      catchError(err => {
        this.trasladoService.checkError(err);
        return throwError(() => err);
      })
    );
  }

  guardarTraslado() {
    this.route.queryParams.subscribe(params => {
      const tokenProceso = params['proceso'];
      if (tokenProceso) {
        this.traslado.tokenProceso = tokenProceso;
      }
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de traslado',
        "Deseas continuar?"
      );

      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") {
              Object.assign(this.traslado, this.informeFormGroup.value);

              // HTML DE DETALLE DE CORREO
              this.traslado.html = `<br>
                                    <strong>Descripción de la solicitud:</strong>${this.traslado.descripcionSolicitud}<br>
                                    <strong>Centro de destino:</strong>${this.traslado.centroDestino.nombre}<br>
                                    <strong>Motivo de traslado:</strong>${this.traslado.motivoTraslado.nombre}<br>
                                    <br>
                                    <strong>Adolescentes:</strong>
                                    <ul>
                                    `;
              for (let adolescente of this.traslado.trasladoAdolescentes) {
                const nombreAdolescente = `${adolescente.fichaIdentificacion.apellidoPaterno} ${adolescente.fichaIdentificacion.apellidoMaterno} ${adolescente.fichaIdentificacion.nombres}`;
                const htmlNombre = `<li>${nombreAdolescente}</li>`;
                this.traslado.html += htmlNombre;
              }
              this.traslado.html += `</ul>
                                    <br>
                                    <strong>Conclusiones:</strong>${this.traslado.conclusiones}<br>
                                    <strong>Recomendaciones:</strong>${this.traslado.recomendaciones}<br>
                                    `;

              this.traslado.html += `<br>
                                    ${this.funcionarioActivo.nombres} ${this.funcionarioActivo.apellidos}<br>
                                    ${this.funcionarioActivo.cargo}<br>
                                    ${this.funcionarioActivo.departamento}
                                    `;

              let tareaTraslado = new TareaTrasladoDTO;
              tareaTraslado.traslado = this.traslado;
              tareaTraslado.tarea = this.tareaEntrante;
              this.traslado.usuarioCreaTraslado = `${this.funcionarioActivo?.nombres ?? ''} ${this.funcionarioActivo?.apellidos ?? ''}`.trim();
              this.trasladoService.crearEditarTraslado(tareaTraslado, '').subscribe(
                {
                  next: (response: RespuestaPorDefecto<TrasladoDTO>) => {

                    if (!response.exito) {
                      this.trasladoService.checkError(response);

                      return;
                    }
                    this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);
                    this.router.navigate([`/flujo-trabajo/bandeja-salida`])

                  },
                  error: (error: any) => {
                    this.trasladoService.checkError(error);
                  }
                }
              )
            }
          }
        }
      );
    })
  }

  async generarPdf() {
    Object.assign(this.traslado, this.informeFormGroup.value);

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
      "[IMG_BASE64]": await this.funcionesUtils.obtenerLogoPdf(),
      "[TITULO-INFORME]": this.tareaEntrante.paso.nombre,
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
      "[SOLICITUD]": this.traslado.descripcionSolicitud
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
  }
}
