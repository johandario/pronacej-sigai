import {  Component, OnInit  } from '@angular/core';
import { FormsModule, UntypedFormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/modules/catalogo/catalogo.service';
import { ActivatedRoute, Router } from '@angular/router';
import { GestionFugaService } from '../gestion-fuga.service';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, provideNativeDateAdapter,DateAdapter } from '@angular/material/core';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { MatButtonModule } from '@angular/material/button';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { catchError, Observable, of,map,tap, lastValueFrom } from 'rxjs';
import { List } from 'lodash';
import { CatalogoService as CatService } from 'app/core/services/catalogo.service';
import { PdfService } from 'app/core/services/pdf.service';
import { CUSTOM_DATE_FORMATS, FuncionesUtils,CustomDateAdapter } from 'app/core/utils/funcionesUtils.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { HttpClient } from '@angular/common/http';
import { CommonModule,Location } from '@angular/common';
import { TareaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { log } from 'console';
import { FuncionarioDTO } from 'app/core/model/both/seguridad/FuncionarioDTO.model';
import { FuncionarioService } from 'app/modules/seguridad/services/funcionario.service';


@Component({
  selector: 'app-fuga-apoderado',
  standalone: true,
  imports: [ 
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatButtonModule,
    FormsModule,
    CommonModule,
  ],
  templateUrl: './fuga-apoderado.component.html',
  styleUrl: './fuga-apoderado.component.scss',
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
    { provide: DateAdapter, useClass: CustomDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    provideNativeDateAdapter(),
  ],
})
export class FugaApoderadoComponent implements OnInit {
  
  tokenID: string;
  fuga: GestionFugaDTO = new GestionFugaDTO();
  isLoading: boolean =  false;
  nemonicoParentezco: string = "PARENTESCO";
  catalogosParentezco: CatalogoDTO[] = [];
  estado: string = '';
  base64Image: string | null = null;
  tareaEntrante: TareaDTO = new TareaDTO;
  fechaISO: string
  fichaIdentifacion: FichaIdentificacionDTO
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_CONTACTO_ADOLESCENTE;
  funcionarioActivo: FuncionarioDTO;
    nemonicoMenuinicio = etiquetasModel.NEMONICO_MENU_INICIO;

  modeloOficio2Form = this.fb.group({
    fechaInformeApoderado: ['', Validators.required],
    apoderado: ['', Validators.required],
    dni: ['', Validators.required],
    parentesco: [null as CatalogoDTO, Validators.required],
    descripcion: ['', Validators.required],
    accionesRealizadas: ['', Validators.required],
  });

  constructor(
    private catalogoService: CatalogoService,
    private router: Router, private route: ActivatedRoute,
    private gestionFugaService: GestionFugaService,
    private fb: UntypedFormBuilder,
    private dialogMensajeService: DialogMensajeService,
    private catService: CatService,
    private pdfService: PdfService,
    private funcionesUtils: FuncionesUtils,
    private http: HttpClient, 
    private _location: Location,  
    private fichaIdentificacionService: FichaIdentificacionService,
    private funcionarioService: FuncionarioService,
    
  ) {}

  ngOnInit() {
    if (history.state.tareaEntrante && history.state.listaTareas) {
      this.tareaEntrante = history.state.tareaEntrante;
         
    }
    this.obtenerTokenDepartamento();
    this.estado= history.state.tareaEntrante.estado
  
    
    this.tokenID = this.route.snapshot.params['tokenID'];
    const tarea =  this.route.snapshot.params['tokenTarea'];
    if (this.tokenID) {
    
      this.gestionFugaService.obtenerFugasPorTokenID(this.tokenID, this.nemonicoMenu).subscribe((result) => {
        this.fuga = result.data;
        console.log(this.fuga);
        // await lastValueFrom(this.obtenerFichaIdentificacion());
        if (this.fuga.fechaInformeApoderado) {
          const fecha = new Date(this.fuga.fechaInformeApoderado);
          this.fechaISO = fecha.toISOString().split('T')[0];
        }
        // this.modeloOficio2Form.disable();
        if (this.fuga.descripcionHechos) {
          this.modeloOficio2Form.patchValue({
            descripcion: this.fuga.descripcionHechos,
            accionesRealizadas: this.fuga.accionesRealizadas,
            apoderado:this.fuga.apoderado,
           
          });
        }
        if (this.fuga.dni) {
          this.modeloOficio2Form.patchValue({
            dni: this.fuga.dni,
          });
        }
        //await this.getCatalogosHijos();

        this.catService.obtenerHijos('PARENTESCO', '').subscribe({
          next: (responseCatalog) => {
            this.catalogosParentezco = responseCatalog.data;

            if (this.fuga.parentesco) {
              const parentescoSeleccionado = this.catalogosParentezco.find(
                (catalogo) => catalogo.tokenIdentificador === this.fuga.parentesco.tokenIdentificador
              );

              if (this.fuga.fechaInformeApoderado) {
                this.modeloOficio2Form.patchValue({
                  parentesco: parentescoSeleccionado,
                  fechaInformeApoderado: this.fuga.fechaInformeApoderado
                    ? new Date(this.fuga.fechaInformeApoderado)
                    : null,
                });
              }
            }

                        
          },
          error: (error) => {
            console.error('Error al obtener catálogos:', error);
          },
        });

        // const parentescoSeleccionado = this.catalogosParentezco.find(
        //   (catalogo) => catalogo.idCatalogo === this.fuga.parentesco.idCatalogo
        // );
        // this.modeloOficio2Form.patchValue({
        //   parentesco: parentescoSeleccionado,
        //   fechaInformeApoderado: this.fuga.fechaInformeApoderado
        //     ? new Date(this.fuga.fechaInformeApoderado)
        //     : null,
        // });
      });
      this.route.queryParams.subscribe((params) => {
        // this.estado = params['estado'];
      });
    }
  }
  

  getCatalogos(nemonico: string): Observable<CatalogoDTO[]> {
    return this.catalogoService.getCatalogosPorNemonicPadre(nemonico,this.nemonicoMenu).pipe(
      map((response: RespuestaPorDefecto<List<CatalogoDTO>>) => {
        if (!response.exito) {
          this.catalogoService.checkError(response);
          return [];
        }
        return Array.from(response.data);
      }),
      catchError((error: any) => {
        this.catalogoService.checkError(error);
        return of([]);
      })
    );
  }

  async getCatalogoParentezco(){
    this.getCatalogos(this.nemonicoParentezco).subscribe((catalogos: CatalogoDTO[]) => {
      this.catalogosParentezco = catalogos;
      console.log(this.catalogosParentezco);
      
    }); 
  }

  cancelar() {
    this._location.back();
    // this.router.navigate([`/flujo-trabajo/bandeja-entrada`]);
  }

   obtenerFichaIdentificacion(): Observable<any> {
          return this.fichaIdentificacionService
          .obtenerFichaIdentificacionPorTokenIdentificador(this.fuga.tokenFichaIdentificacion, this.nemonicoMenu)
          .pipe(
              tap((response) => {
                console.log("Ficha de Identificación cargada:", response.data);
                this.fichaIdentifacion = response.data
              }),
              catchError((error) => {
                console.error("Error al obtener ficha de identificación:", error);
                return of(null); 
              })
          );
        }
  
  guardarFuga(){
    console.log(this.fichaIdentifacion);
    
    this.route.queryParams.subscribe(params => {
      const tokenInstancia = params['instancia'];
      if (tokenInstancia) {
        this.fuga.tokenInstancia = tokenInstancia;
      } else {
        this.fuga.tokenInstancia = this.tokenID;
      }
      this.fuga.ultimoPaso= true
      console.log(this.fuga);
      
      this.modeloOficio2Form.markAllAsTouched();
      if (this.modeloOficio2Form.invalid) {
        this.dialogMensajeService.mensajeError('Por favor, completa todos los campos obligatorios antes de guardar.');
        return;
      }
      console.log(this.fuga);
      
      let ref = this.dialogMensajeService.mensajeConConfirmacion(
        'Se creará un registro de fuga',
        "Deseas continuar?"
      );
      ref.afterClosed().subscribe(
        {
          next: (resp: "confirmed" | "cancelled") => {
            if (resp == "confirmed") { 
              Object.assign(this.fuga, this.modeloOficio2Form.value);
            
              this.fuga.html = `
              <br><strong>Fecha de la fuga: </strong>${this.fuga.fechaFuga}<br>
             <strong>Adolescente: </strong>${this.fuga.nombreAdolescente} <strong>Cédula:</strong> ${this.fuga.numeroIdentificacion}<br><br>
              <strong>Presencia de: </strong>${this.fuga.presenciaDe}<br><br>
              <strong>Narrar los hechos: </strong>${this.fuga.descripcionHechos}<br>  `;

              const tareaEventoFuga = {
                eventoFuga: this.fuga,
                tarea: this.tareaEntrante
              };

              console.log(this.fuga);
              
              this.gestionFugaService.crearEditarFuga(tareaEventoFuga, '').subscribe(
                {
                  next: (response: RespuestaPorDefecto<GestionFugaDTO>) => {
                    if (!response.exito) {
                      this.gestionFugaService.checkError(response);
                      return;
                    }                  
                    this.dialogMensajeService.mensajeExitoso(response.titulo, response.mensaje);     
                    this.router.navigate([`/flujo-trabajo/bandeja-entrada`])
                  },
                  error: (error: any) => {
                    this.gestionFugaService.checkError(error);
                  }
                }
              )
            }
          }
        }
      );
    })
  }

  // getCatalogo (event: any):void{
  //   const nemonico = event.value
  //   console.log(nemonico);
    
  //   this.catService.obtenerCatalogoPorNemonico(
  //     nemonico,
  //     ''
  //   ).subscribe(
  //     {
  //       next: (respuesta: RespuestaPorDefecto<CatalogoDTO>) => {
  //         console.log('Respuesta del servicio:', respuesta);
  //         if (!respuesta.exito) {

            
  //           this.catalogoService.checkError(respuesta);
  //           return;
  //         }
  //       },
  //       error: (error: any) => {
  //         this.catalogoService.checkError(error);
  //       }
  //     }
  //   );
  // }

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

  pruebaPdf() {
    this.loadImageAsBase64();
    setTimeout(() => {
    const fechaRegistro = this.formatFecha((new Date).toString())
    const horaRegistro= this.formatHora((new Date).toString())
    const titulopantala= "Informe de fuga"
    Object.assign(this.fuga, this.modeloOficio2Form.value);
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
      "[CENTRO]": this.funcionarioActivo.departamento,
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


  getCatalogosHijos(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.catService.obtenerHijos('PARENTESCO', '').subscribe({
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
  
  actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
    if (event.value) {
      const fecha = event.value;
      this.modeloOficio2Form.get(controlName).setValue(fecha);
    }
  }

  soloNumeros(event: KeyboardEvent) {
    const charCode = event.key.charCodeAt(0);
    if (charCode < 48 || charCode > 57) {
      event.preventDefault(); 
    }
  } 

  onFechaManual(event: any) {
    const valorIngresado = event.target.value;
    if (valorIngresado) {
        const partes = valorIngresado.split('/');
        if (partes.length === 3) {
          const fechaConvertida = new Date(Number(partes[2]), Number(partes[1]) - 1, Number(partes[0]));

            if (!isNaN(fechaConvertida.getTime())) {
                this.modeloOficio2Form.patchValue({ fechaInformeApoderado: fechaConvertida });
                this.modeloOficio2Form.get('fechaInformeApoderado')?.updateValueAndValidity();
                console.log("Fecha manual válida, establecida en el formulario:", fechaConvertida);
            } else {
                console.warn("Fecha ingresada no válida");
                this.modeloOficio2Form.get('fechaInformeApoderado')?.setErrors({ invalid: true });
            }
        }
    }
}

obtenerTokenDepartamento(): Promise<void> {
    return new Promise((resolve) => {
      this.funcionarioService.obtenerFuncionarioDelUsuario(this.nemonicoMenuinicio).subscribe({
        next: (response: RespuestaPorDefecto<FuncionarioDTO>) => {
          if (!response.exito) {
            resolve();
            return;
          }
          this.funcionarioActivo = response.data;
          console.log(this.funcionarioActivo);
          resolve();
        },
        error: (error: any) => {
          console.error('Error al obtener el departamento:', error);
          resolve();
        }
      });
    });
  }
}
