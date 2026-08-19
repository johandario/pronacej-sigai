import { CommonModule, Location } from '@angular/common';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { PlantillaInformeDTO } from 'app/core/model/both/informe/plantillaInformeDTO.model';
import { InformeService } from '../../services/informe.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { InformeDTO } from 'app/core/model/both/informe/informeDTO.model';
import { ValorInformeDTO } from 'app/core/model/both/informe/valorInformeDTO.model';
import { InformeComponent } from 'app/core/components/informe/informe.component';
import { ActivatedRoute, Router } from '@angular/router';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { MatIconModule } from '@angular/material/icon';
import { PdfService } from 'app/core/services/pdf.service';
import { map, Observable, of, startWith } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { SubidaDeDocumentosComponent } from 'app/core/components/documentos/subida-de-documentos/subida-de-documentos.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { TipoDeDocumento } from 'app/core/components/documentos/modelos/TipoDeDocumento.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { DocumentoSubido } from 'app/core/components/documentos/modelos/DocumentoSubido.model';
import { InformeDocumentoDTO } from 'app/core/model/both/informe/informeDocumentoDTO.model';
import { TipoDeIdentificacionTipoDeDocumentoService } from 'app/core/services/fichaIdentificacionTipoDeDocumento.service';
import { FichaIdentificacionTipoDeDocumentoDTO } from 'app/core/model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { HttpClient } from '@angular/common/http';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { PageEvent } from '@angular/material/paginator';
import { DocumentosSubidosTablaComponent } from 'app/core/components/documentos/documentos-subidos-tabla/documentos-subidos-tabla.component';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';

@Component({
  selector: 'app-informes-crear-editar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatExpansionModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatIconModule,
    MatSelectModule,
    MatTableModule,
    InformeComponent,
    SubidaDeDocumentosComponent,
    DocumentosSubidosTablaComponent
  ],
  templateUrl: './informes-crear-editar.component.html',
  styleUrl: './informes-crear-editar.component.scss'
})

export class InformesCrearEditarComponent implements OnInit, AfterViewInit {
  @ViewChild(InformeComponent) informe!: InformeComponent;

  @ViewChild('documentosComp')
  tablaDocumentos: DocumentosSubidosTablaComponent;

  esEdicion: boolean = false;
  impreso: boolean;
  firmado: boolean;
  uuid_fp: string;

  item: InformeDTO;
  listaPrev: boolean = false;

  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME;
  titulo: String = "Informe";

  base64Image: string | null = null;

  funcionarioActivo: FuncionarioDTO;
  tiposDeDocumentosSistema: TipoDeDocumento[] = [];
  adolescentes: FichaIdentificacionDTO[] = [];
  adolescentesFiltrados: Observable<FichaIdentificacionDTO[]>;
  personaControl = new FormControl();

  tiposInforme: PlantillaInformeDTO[] = [];
  initialForm: FormGroup;
  buttonEnable = true;

  nemonicoInforme: string = "";

  constructor(
    private fb: FormBuilder,
    private location: Location,
    private informeService: InformeService,
    private catalogoService: CatalogoService,
    private fichaService: FichaIdentificacionService,
    private funcionarioService: FuncionarioService,
    private dialogMensajeService: DialogMensajeService,
    private tipoDeIdentificacionTipoDeDocumentoService: TipoDeIdentificacionTipoDeDocumentoService,
    private funcionesUtils: FuncionesUtils,
    private pdfService: PdfService,
    private http: HttpClient,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.listaPrev = history.state.listaPrev;

    this.uuid_fp = this.route.snapshot.paramMap.get('uuid_fp');

    this.initialForm = this.fb.group({
      persona: ['', Validators.required],
      informe: ['', Validators.required]
    });

    this.item = history.state.item;

    if (this.item) {
      this.esEdicion = true;

      if (this.item.impreso)
        this.impreso = true;

      if (this.item.firmado)
        this.firmado = true;
    }

    if (this.esEdicion) {
      this.initialForm.controls.informe.setValue(this.item.idPlantillaInforme);
      this.initialForm.controls.informe.disable();

      this.buttonEnable = true;
    }

    this.obtenerFuncionario();
    this.obtenerTiposDeDocumentos();

    // Sincroniza el FormControl del formulario principal con el de autocompletar
    this.personaControl.valueChanges.subscribe(value => {
      const personaSeleccionada = typeof value === 'string' ? null : value;
      this.initialForm.patchValue({ persona: personaSeleccionada?.idFichaIdentificacion || '' });
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.obtenerDocumentos();
    });
  }

  obtenerDocumentos() {
    let page = this.tablaDocumentos.page;
    let pageSize = this.tablaDocumentos.pageSize;

    let paginacionRequest = new PaginacionRequest();
    paginacionRequest.page = page;
    paginacionRequest.size = pageSize;
    paginacionRequest.tokenIdentificador = this.item.tokenIdentificador;

    this.informeService.obtenerDocumentos(
      paginacionRequest,
      this.nemonicoMenu
    )
      .subscribe({
        next: (
          response: RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        ) => {
          console.log(response);
          if (!response.exito) {
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
            );
            return;
          }

          if (response.data?.data) {
            this.tablaDocumentos.actualizarTabla(
              response.data.data,
              response.data.totalItems
            );
          }
        },
        error: (error: any) => {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los documentos. Inténtalo de nuevo.'
          );
        },
      });
  }

  guardar() {
    if (this.esEdicion)
      this.editarInforme(false);
    else
      this.crearInforme();
  }

  cancelar() {
    if (this.listaPrev)
      this.location.back();
    else
      this.initialForm.reset();
  }

  crearInforme() {
    let informeDTO = new InformeDTO();

    informeDTO.idFichaIdentificacion = this.initialForm.controls.persona.value;
    informeDTO.idPlantillaInforme = this.initialForm.controls.informe.value;
    informeDTO.impreso = true;
    informeDTO.valores = this.obtenerValores();

    this.informeService.crearInforme(informeDTO, this.nemonicoMenu).subscribe({
      next: () => {
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          'Registro guardado correctamente.'
        ).afterClosed().subscribe(() => {
          if (this.listaPrev)
            this.location.back();
          else
            this.initialForm.reset();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  editarInforme(imprimir: boolean) {
    let informeDTO = this.item;
    informeDTO.valores = this.obtenerValores();

    this.informeService.actualizarInforme(informeDTO, this.nemonicoMenu).subscribe({
      next: () => {

        if (!imprimir) {
          this.dialogMensajeService.mensajeExitoso(
            'Editar',
            'Registro actualizado correctamente.'
          ).afterClosed().subscribe(() => {
            this.location.back();
          });
        }
        else {
          this.imprimir();
        }
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al actualizar el registro. Inténtalo de nuevo.'
        );
      }
    });
  }

  onReportTypeChange(event: MatSelectChange) {
    this.nemonicoInforme = this.tiposInforme.find(x => x.idPlantillaInforme == event.value).nemonico;
  }

  cargarTiposInforme() {
    this.informeService.obtenerPlantillas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
      next: (response: RespuestaPorDefecto<PlantillaInformeDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.tiposInforme = response.data;
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  cargarAdolescentes() {
    // this.fichaService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
    this.fichaService.obtenerNombresFichas(this.nemonicoMenu, null).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.adolescentes = response.data;

        // Inicializar filtro
        this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
          startWith(''),
          map(value => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
          map(name => (name ? this._filter(name) : this.adolescentes.slice()))
        );

        if (this.uuid_fp) {
          let adolescente = this.adolescentes.find(x => x.tokenIdentificador == this.uuid_fp);
          this.personaControl.setValue(adolescente);
          this.personaControl.disable();
          this.initialForm.controls.persona.setValue(adolescente.idFichaIdentificacion);
        }
        else if (this.esEdicion) {
          let adolescente = this.adolescentes.find(x => x.idFichaIdentificacion == this.item.idFichaIdentificacion);
          this.personaControl.setValue(adolescente);
          this.personaControl.disable();
          this.initialForm.controls.persona.setValue(adolescente.idFichaIdentificacion);
        }
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  cargarAdolescenteCargado() {
    // this.fichaService.obtenerNombresFichas(this.nemonicoMenu, this.funcionarioActivo.tokenIdentificadorDepartamento).subscribe({
    this.fichaService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.adolescentes = [];
        this.adolescentes.push(response.data);

        // Inicializar filtro
        this.adolescentesFiltrados = this.personaControl.valueChanges.pipe(
          startWith(''),
          map(value => (typeof value === 'string' ? value : this.getNombreCompleto(value))),
          map(name => (name ? this._filter(name) : this.adolescentes.slice()))
        );

        if (this.uuid_fp) {
          let adolescente = this.adolescentes.find(x => x.tokenIdentificador == this.uuid_fp);
          this.personaControl.setValue(adolescente);
          this.personaControl.disable();
          this.initialForm.controls.persona.setValue(adolescente.idFichaIdentificacion);
        }
        else if (this.esEdicion) {
          let adolescente = this.adolescentes.find(x => x.idFichaIdentificacion == this.item.idFichaIdentificacion);
          this.personaControl.setValue(adolescente);
          this.personaControl.disable();
          this.initialForm.controls.persona.setValue(adolescente.idFichaIdentificacion);
        }
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  confirmarImpresion(): void {
    this.loadImageAsBase64();

    if (!this.item.impreso) {
      this.item = { ...this.item, impreso: true }; // Nueva referencia para que detecte el cambio
      this.editarInforme(true);
      this.imprimir();
    }
    else
      this.imprimir();
  }

  imprimir() {
    this.fichaService.obtenerFichaIdentificacionPorId(this.item.idFichaIdentificacion, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaDTO = response.data;

          // Construir los nuevos campos dinámicamente
          const lugarFechaNacimiento = `${fichaDTO.lugarNacimiento || ''}, ${this.formatFecha(fichaDTO.fechaNacimiento)}`;
          const edadActual = this.funcionesUtils.getEdad(fichaDTO.fechaNacimiento).toString() || 'N/A';
          const gradoInstruccion = fichaDTO.gradoInstruccion || 'N/A';
          const direccion = fichaDTO.direccion || 'N/A';

          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_INFORME;
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[FECHA_REGISTRO]": this.formatFecha(this.item.fechaRegistro.toString()),
            "[HORA_REGISTRO]": this.formatHora(this.item.fechaRegistro.toString()),
            "[TITULO-INFORME]": this.item.tipo,
            "[ADOLESCENTE]": this.item.asignado,
            "[LUGAR_FECHA_NACIMIENTO]": lugarFechaNacimiento,
            "[CENTRO]": fichaDTO.centroIngreso,
            "[EDAD_ACTUAL]": edadActual,
            "[GRADO_INSTRUCCION]": gradoInstruccion,
            "[DIRECCION]": direccion,
            "[INFORME]": this.item.idInforme.toString(),
          }
          this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
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
                'Hubo un problema al generar el archivo. Inténtalo de nuevo.'
              );
            }
          });

        },
        error: (error: any) => {
          this.fichaService.checkError(error);
        }
      }
    );
  }

  subirInforme(documentos: DocumentoSubido[]) {
    if (documentos.length > 1) {
      this.dialogMensajeService.mensajeError(
        'Solo 1 documento permitido.'
      );
      return;
    }
    let documentoSubido = documentos.at(0);

    //TODO: GUARDAR REGISTRO DE MANDATO ANTES DE SUBIR UN ARCHIVO
    if (documentoSubido) {

      let informeDTO = this.item;
      informeDTO.informeDocumentoDTO = new InformeDocumentoDTO();
      informeDTO.informeDocumentoDTO.documentoDTO = documentoSubido.documentoDTO;

      let load = this.dialogMensajeService.mensajeLoading("Subiendo el documento: " +
        documentoSubido.documentoDTO.nombre
      );
      this.informeService.subirInformeFirmado(
        informeDTO,
        documentoSubido.documento,
        this.nemonicoMenu
      ).subscribe(
        {
          next: (response: RespuestaPorDefecto<Boolean>) => {

            load.close();
            if (!response.exito) {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al subir el documento. Inténtalo de nuevo.'
              );
              return;
            }

            this.dialogMensajeService.mensajeExitoso(
              'Subir',
              'Documento subido correctamente.'
            ).afterClosed().subscribe(() => {
              this.obtenerDocumentos();
            });
          },
          error: (error: any) => {
            load.close();
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al subir el documento. Inténtalo de nuevo.'
            );
          }
        }
      );
    } else {
      this.dialogMensajeService.mensajeError("No se obtuvo documento para ser subido");
    }
  }

  cambioAdolescente(adolescente: FichaIdentificacionDTO): void {
    // Cuando seleccionas una opción, sincroniza el ID en el formulario principal
    this.initialForm.patchValue({ persona: adolescente.idFichaIdentificacion });
  }

  displayFn = (adolescente: FichaIdentificacionDTO): string => {
    return adolescente ? this.getNombreCompleto(adolescente) : '';
  }

  private _filter(name: string): any[] {
    const filterValue = name.toLowerCase();
    return this.adolescentes.filter(adolescente =>
      this.getNombreCompleto(adolescente).toLowerCase().includes(filterValue)
    );
  }

  onInputFocus(): void {
    const inputElement = (document.activeElement as HTMLInputElement);
    inputElement.select(); // Selecciona todo el texto
  }

  getNombreCompleto(adolescente: FichaIdentificacionDTO): string {
    return `${adolescente.apellidoPaterno} ${adolescente.apellidoMaterno} ${adolescente.nombres}`;
  }

  obtenerValores(): ValorInformeDTO[] {
    const valores: ValorInformeDTO[] = [];

    Object.keys(this.informe.formulario.controls).forEach(key => {
      const control = this.informe.formulario.get(key);

      // Crear el ValorInformeDTO para cada campo
      const valorInformeDTO: ValorInformeDTO = {
        idCampo: +key,
        valor: String(control?.value || '')
      };

      valores.push(valorInformeDTO);
    });

    return valores;
  }

  obtenerTiposDeDocumentos() {
    this.tipoDeIdentificacionTipoDeDocumentoService.obtenerTiposDeDocumentos(etiquetasModel.SECCION_FICHA_IDENT_INFORMES,
      this.nemonicoMenu).subscribe(
        {
          next: (response: RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>) => {

            if (!response.exito) {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
              return;
            }

            let tiposArchivos = response.data;

            if (tiposArchivos.length == 0) {
              this.dialogMensajeService.mensajeError("No se ha configurado los tipos de documentos para esta sección");
              return;
            }

            this.tiposDeDocumentosSistema =
              tiposArchivos.map(
                (tipoArch) => {
                  let catalogoTipoDoc = tipoArch.tipoArchivoSistemaDTO;
                  let tipoDeDocumento = new TipoDeDocumento();
                  tipoDeDocumento.tokenIdentificador = catalogoTipoDoc.tokenIdentificador;
                  tipoDeDocumento.nemonico = catalogoTipoDoc.nemonico;
                  tipoDeDocumento.requerido = tipoArch.requerido;
                  tipoDeDocumento.descripcion = catalogoTipoDoc.descripcion;
                  tipoDeDocumento.nombre = catalogoTipoDoc.nombre;

                  return tipoDeDocumento;
                }
              );
          },
          error: (error: any) => {
            this.tipoDeIdentificacionTipoDeDocumentoService.checkError(error);
          }
        }
      );
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

  // Función para cargar la imagen como base64
  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  // Función para convertir el ArrayBuffer a base64
  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }

  obtenerFuncionario() {
    this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {

          if (!response.exito) {
            return;
          }

          this.funcionarioActivo = response.data;
          if (this.esEdicion) {
            this.cargarAdolescenteCargado();
          } else {
            this.cargarAdolescentes();            
          }
          this.cargarTiposInforme();
        },
        error: (error: any) => {
          console.log('Hubo un problema al recuperar el funcionario activo. Inténtalo de nuevo.');
        }
      }
    );
  }

  pageEventDocumentos(event: PageEvent) {
    this.tablaDocumentos.page = event.pageIndex;
    this.tablaDocumentos.pageSize = event.pageSize;

    this.obtenerDocumentos();
  }
}
