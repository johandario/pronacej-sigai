import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { PersonaRelacionadaDTO } from 'app/core/model/both/PersonaRelacionadaDTO.model';
import { PersonaRelacionadaEnfermedadDTO } from 'app/core/model/both/personaRelacionadaEnfermedadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { debounceTime, distinctUntilChanged, map, Observable, of, startWith, switchMap } from 'rxjs';
import { ModalCrearParentescoComponent } from '../modal-crear-parentesco/modal-crear-parentesco.component';
import { ClasificacionEnfermedadService } from 'app/core/services/clasificacion-enfermedad.service';
import { ClasificacionEnfermedadDTO, ClasificacionEnfermedadRequest } from 'app/core/model/both/clasificacionEnfermedadDTO.model';
import {MatRadioModule} from '@angular/material/radio';
import { autocompleteObjectValidator, noWhitespaceValidator } from 'app/core/utils/CustomValidators.validator';

@Component({
  selector: 'app-modal-crear-editar-enfermedad-persona-relacionada',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    CommonModule,
    MatSlideToggleModule,
    MatAutocompleteModule,
    MatRadioModule
  ],
  templateUrl: './modal-crear-editar-enfermedad-persona-relacionada.component.html',
  styleUrl: './modal-crear-editar-enfermedad-persona-relacionada.component.scss'
})
export class ModalCrearEditarEnfermedadPersonaRelacionadaComponent implements OnInit {
  modoVisualizacion: boolean = false;

  listaSexoParentesco: CatalogoDTO[] = [];
  listaTipoEnfermedades: ClasificacionEnfermedadDTO[] = [];
  listaTipoParentescos: CatalogoDTO[] = [];
  personasRelacionadas: PersonaRelacionadaDTO[] = [];

  tiposParentescoFiltrado: Observable<CatalogoDTO[]>;
  tiposEnfermedadFiltrado: Observable<ClasificacionEnfermedadDTO[]>;

  clasificacionEnfermedadRequest: ClasificacionEnfermedadRequest = {
    valor: '',
    sexo: null
  };

  ingresoEnfermedadPersonaForm = this.fb.group({
    clasificacionEnfermedad: [null as ClasificacionEnfermedadDTO, [autocompleteObjectValidator(), Validators.required]],
    detalle: [null, [noWhitespaceValidator()]],
    activo: [false, []],
    // personaRelacionada: [null, []],
    tipoParentesco: [null as CatalogoDTO, []],
    sexoParentesco: [null as CatalogoDTO, [Validators.required]],
    id_temporal: []
  });

  uuid_fp: string;

  constructor(
    private fb: FormBuilder,
    private cd: ChangeDetectorRef,
    private catalogoService: CatalogoService,
    private clasificacionEnfermedadService: ClasificacionEnfermedadService,
    public dialogRef: MatDialogRef<ModalCrearEditarEnfermedadPersonaRelacionadaComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils,
    private route: ActivatedRoute,
    private datosFamiliaresServices: DatosFamiliaresService,
    private dialog: MatDialog
  ) {
    if (data.modoVisualizacion) {
      this.modoVisualizacion = data.modoVisualizacion;
      this.ingresoEnfermedadPersonaForm.disable();
    }
  }

  async ngOnInit(): Promise<void> {
    this.uuid_fp = this.data.uuid_fp;
    // await this.encontrarPersonasRelacionadasPorFichaToken()
    await this.cargarCatalogos();
    if (this.data.informacion) {
      console.log('informacion desde pag', this.data.informacion)
      // this.ingresoEnfermedadPersonaForm.get('clasificacionEnfermedad').setValue(this.data.informacion.tokenTipoEnfermedad);
      this.ingresoEnfermedadPersonaForm.get('detalle').setValue(this.data.informacion.detalle);
      this.ingresoEnfermedadPersonaForm.get('activo').setValue(this.data.informacion.enfermedadActiva);
      // this.ingresoEnfermedadPersonaForm.get('personaRelacionada').setValue(this.data.informacion.tokenIdentificadorPersona);
      // this.ingresoEnfermedadPersonaForm.get('tipoParentesco').setValue(this.data.informacion.tipoParentesco);
      if (this.data.informacion.id_temporal) {
        this.ingresoEnfermedadPersonaForm.get('id_temporal').setValue(this.data.informacion.id_temporal);
      }
    }
  }

  async cargarCatalogos() {
    this.funcionesUtils.obtenerListaCatalogo('PARENTESCO', "").subscribe({
      next: (data) => {
        this.listaTipoParentescos = data;   
        this.listaTipoParentescos.sort((a, b) => {           
          return a.nombre.toLocaleLowerCase().localeCompare(b.nombre.toLocaleLowerCase());
        });
        
        const controlParentesco = this.ingresoEnfermedadPersonaForm.get('tipoParentesco');
        
        if (this.data.informacion && this.data.informacion.tipoParentesco) {
          const parentesco = this.listaTipoParentescos.find(
            p => p.tokenIdentificador === this.data.informacion.tipoParentesco.tokenIdentificador
          );

          controlParentesco
            .setValue(parentesco);
        }

        this.tiposParentescoFiltrado = controlParentesco.valueChanges.pipe(
          startWith(controlParentesco?.value || ''),
          map(value => {
            const results = this._filter(value, this.listaTipoParentescos);

            if (results.length === 0 && value) {
              const nuevo: CatalogoDTO = {
                tokenIdentificador: '__add_new__',
                descripcion: '',
                nemonico: `${value}`,
                nombre: `Agregar "${value}"`,
                esEdicion: true
              };

              return [nuevo];
            }

            return results;
          })
        );  

      },
      error: (error) => console.error('Error cargando grados de instrucción:', error)
    });

    this.funcionesUtils.obtenerListaCatalogo('TIPO_SEXO', "").subscribe({
      next: (data) => {
        this.listaSexoParentesco = data;

        const controlSexo = this.ingresoEnfermedadPersonaForm.get('sexoParentesco');
        
        if (this.data.informacion && this.data.informacion.sexoParentesco) {
          const sexoParentesco = this.listaSexoParentesco.find(
            s => s.tokenIdentificador === this.data.informacion.sexoParentesco.tokenIdentificador
          );

          controlSexo.setValue(sexoParentesco, { emitEvent: false });
        }

        this.clasificacionEnfermedadRequest.sexo = this.obtenerFiltroSexo(controlSexo.value);
        this.cargarTiposEnfermedades(true);

        controlSexo.valueChanges.subscribe((sexoParentesco: CatalogoDTO) => {
          this.clasificacionEnfermedadRequest.sexo = this.obtenerFiltroSexo(sexoParentesco);
          this.clasificacionEnfermedadRequest.valor = '';
          this.ingresoEnfermedadPersonaForm.get('clasificacionEnfermedad').reset(null, { emitEvent: false });
          this.cargarTiposEnfermedades(false);
        });
      },
      error: (error) => console.error('Error cargando grados de instrucción:', error)
    });
  }

  private obtenerFiltroSexo(sexoParentesco: CatalogoDTO): string {
    return sexoParentesco?.nombre?.toUpperCase() || null;
  }

  private cargarTiposEnfermedades(asignarValorEdicion: boolean): void {
    const controlEnfermedad = this.ingresoEnfermedadPersonaForm.get('clasificacionEnfermedad');

    if (asignarValorEdicion && this.data.informacion?.clasificacionEnfermedad) {
      this.clasificacionEnfermedadRequest.valor = this.data.informacion.clasificacionEnfermedad.nombre;
    }

    if (!this.clasificacionEnfermedadRequest.sexo) {
      this.listaTipoEnfermedades = [];
      this.configurarFiltroEnfermedad(controlEnfermedad);
      return;
    }

    this.clasificacionEnfermedadService.obtenerClasificacionEnfermerdades(this.clasificacionEnfermedadRequest, '').subscribe({
      next: (response) => {
        this.listaTipoEnfermedades = response.data;

        if (asignarValorEdicion && this.data.informacion?.clasificacionEnfermedad) {
          const clasificacion = this.listaTipoEnfermedades.find(
            c => c.tokenIdentificador === this.data.informacion.clasificacionEnfermedad.tokenIdentificador
          );
          controlEnfermedad.setValue(clasificacion);
        }

        this.configurarFiltroEnfermedad(controlEnfermedad);
      },
      error: (error) => console.error('Error cargando grados de instrucción:', error)
    });
  }

  private configurarFiltroEnfermedad(controlEnfermedad: any): void {
    this.tiposEnfermedadFiltrado = controlEnfermedad.valueChanges.pipe(
      startWith(controlEnfermedad?.value || ''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((value: string | ClasificacionEnfermedadDTO) => {
        const texto = typeof value === 'string' ? value : value?.nombre;
        this.clasificacionEnfermedadRequest.valor = texto || '';

        if (!this.clasificacionEnfermedadRequest.sexo) {
          return of({ data: [] as ClasificacionEnfermedadDTO[] });
        }

        return this.clasificacionEnfermedadService
          .obtenerClasificacionEnfermerdades(this.clasificacionEnfermedadRequest, '');
      }),
      map((response: any) => response.data || [])
    );
  }

  private _filter(value: any | string, lista: any[]): any[] {
    const filterValue =
      typeof value === 'string'
        ? value.toLowerCase()
        : value?.nombre?.toLowerCase() || '';

    return lista.filter(option =>
      option.nombre.toLowerCase().includes(filterValue)
    );
  }

  private _filterEnfermedad(value: any | string): any[] {
    const filterValue = typeof value === 'string'
        ? value.toLowerCase()
        : value?.nombre?.toLowerCase() || '';

    return this.listaTipoEnfermedades.filter(option => {
      const nombreCompleto = `${option.codigo} | ${option.nombre}`.toLowerCase();
      return nombreCompleto.includes(filterValue)
    })
  }

  displayFn(option: CatalogoDTO): string {
    return option && option.nombre ? option.nombre : '';
  }

  displayFnEnfermedad(option: ClasificacionEnfermedadDTO): string {
    return option && option.codigo && option.nombre
      ? `${option.codigo} | ${option.nombre}`
      : '';
  }

  onOptionSelected(event: MatAutocompleteSelectedEvent) {
    const option = event.option.value;

    if (option.esEdicion) {
      const confirmarCreacion = this.dialogMensajeService.mensajeConConfirmacion(
        // 'Confirmar creación',
        `¿Deseas crear el parentesco "${option.nemonico}"?`,
        'Se agregará un nuevo tipo de parentesco con el nombre ingresado.'
      );
      confirmarCreacion.afterClosed().subscribe(result => {
        this.ingresoEnfermedadPersonaForm.get('tipoParentesco').reset();
        if (result === 'confirmed') {
          // Crear el nuevo parentesco y agregarlo a la lista
          this.catalogoService.crearCatalogo({
            nombre: option.nemonico,
            descripcion: null,
            nemonico: null,
            tokenIdentificadorPadre: this.listaTipoParentescos[0]?.tokenIdentificadorPadre // Asignar el mismo padre que los existentes
          }, "").subscribe({
            next: (response) => {
              if (response.exito && response.data) {
                this.listaTipoParentescos.push(response.data);
                this.listaTipoParentescos.sort((a, b) => a.nombre.localeCompare(b.nombre));

                const controlParentesco = this.ingresoEnfermedadPersonaForm.get('tipoParentesco');        

                controlParentesco.setValue(response.data);

                this.tiposParentescoFiltrado = controlParentesco.valueChanges.pipe(
                  startWith(controlParentesco?.value || ''),
                  map(value => {
                    const results = this._filter(value, this.listaTipoParentescos);
                    return results;
                  })
                );  

              }
            }
          });

        } 
      });
      // this.abrirModalCrear(option.label);
    } 
  }

  abrirModalCrear(texto: string) {
    const dialogRef = this.dialog.open(ModalCrearParentescoComponent, {
      data: { nombre: texto }

    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Agregar a lista y seleccionarlo automáticamente        
      }
    });
  }

  async encontrarPersonasRelacionadasPorFichaToken() {
    let fichaDTO = new FichaIdentificacionDTO();
    fichaDTO.tokenIdentificador = this.uuid_fp;
    this.datosFamiliaresServices.obtenerPersonasRelacionadasPorTokenFicha(fichaDTO, "").subscribe({
      next: (response: RespuestaPorDefecto<PersonaRelacionadaDTO[]>) => {
        if (!response.exito) {
          if(!response.data){
            this.dialogMensajeService.mensajeError(
              'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
            );
            
          }
          return;
        }

        this.personasRelacionadas = response.data;
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  registrarEnfermedad() {
    if (this.ingresoEnfermedadPersonaForm.valid) {
      let enfermedadPersona = new PersonaRelacionadaEnfermedadDTO()
      enfermedadPersona.detalle = this.ingresoEnfermedadPersonaForm.get('detalle').value;
      // enfermedadPersona.tokenIdentificadorPersona = this.ingresoEnfermedadPersonaForm.get('personaRelacionada').value;
      enfermedadPersona.tipoParentesco = this.ingresoEnfermedadPersonaForm.get('tipoParentesco').value;
      enfermedadPersona.sexoParentesco = this.ingresoEnfermedadPersonaForm.get('sexoParentesco').value;
      enfermedadPersona.enfermedadActiva = this.ingresoEnfermedadPersonaForm.get('activo').value;
      enfermedadPersona.clasificacionEnfermedad = this.ingresoEnfermedadPersonaForm.get('clasificacionEnfermedad').value;
      // enfermedadPersona.tokenTipoEnfermedad = this.ingresoEnfermedadPersonaForm.get('tipoEnfermdad').value;
      //enfermedadPersona.nombreEnfermedad = this.listaTipoEnfermedades.find(x => x.tokenIdentificador == enfermedadPersona.tokenTipoEnfermedad).nombre
      // enfermedadPersona.nombrePersona = this.personasRelacionadas.find(x => x.tokenIdentificador == enfermedadPersona.tokenIdentificadorPersona).nombres
      // enfermedadPersona.parentescoPersona = this.personasRelacionadas.find(x => x.tokenIdentificador == enfermedadPersona.tokenIdentificadorPersona).tipoParentesco
      
      if (this.data.informacion) {
        if(this.data.informacion.tokenIdentificador){
          enfermedadPersona.tokenIdentificador = this.data.informacion.tokenIdentificador;
        }
        enfermedadPersona.esEdicion = true;
        enfermedadPersona.id_temporal = this.ingresoEnfermedadPersonaForm.get('id_temporal').value

      } else {
        enfermedadPersona.id_temporal = Date.now();
      }
      this.dialogRef.close(enfermedadPersona);
    }
  }

  cerrar() {
    this.dialogRef.close(false);
  }

}
