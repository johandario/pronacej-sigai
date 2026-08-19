import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerInputEvent, MatDatepickerModule } from '@angular/material/datepicker';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogContent,
    MatDialogModule,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { FichaMedicaEnfermedadDTO } from 'app/core/model/both/fichaMedicaEnfermedadDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CustomPaginatorIntl } from 'app/core/services/custom-paginator-intl.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { DatosFamiliaresService } from 'app/modules/seguridad/services/datosFamiliares.service';
import { CustomDateAdapter, CUSTOM_DATE_FORMATS, FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { DateAdapter, MAT_DATE_LOCALE, provideNativeDateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { ClasificacionEnfermedadDTO, ClasificacionEnfermedadRequest } from 'app/core/model/both/clasificacionEnfermedadDTO.model';
import { ClasificacionEnfermedadService } from 'app/core/services/clasificacion-enfermedad.service';
import { debounceTime, distinctUntilChanged, map, Observable, startWith, switchMap } from 'rxjs';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { autocompleteObjectValidator } from 'app/core/utils/CustomValidators.validator';

@Component({
    selector: 'app-modal-crear-editar-enfermedad-ficha',
    standalone: true,
    imports: [
        MatFormFieldModule,
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
        MatDatepickerModule,
        MatAutocompleteModule
    ],
    templateUrl: './modal-crear-editar-enfermedad-ficha.component.html',
    styleUrl: './modal-crear-editar-enfermedad-ficha.component.scss',
    providers: [
        { provide: MAT_DATE_LOCALE, useValue: 'es' },
        { provide: DateAdapter, useClass: CustomDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS }
    ],
})
export class ModalCrearEditarEnfermedadFichaComponent implements OnInit {
    listaTipoEnfermedades: ClasificacionEnfermedadDTO[] = [];
    tiposEnfermedadFiltrado: Observable<ClasificacionEnfermedadDTO[]>;

    ingresoEnfermedadFichaForm : FormGroup;    

    constructor(
        private fb: FormBuilder,
        private cd: ChangeDetectorRef,
        private catalogoService: CatalogoService,
        public dialogRef: MatDialogRef<ModalCrearEditarEnfermedadFichaComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private dialogMensajeService: DialogMensajeService,
        public funcionesUtils: FuncionesUtils,
        private route: ActivatedRoute,
        private datosFamiliaresServices: DatosFamiliaresService,
        private dateAdapter: DateAdapter<any>,
        private clasificacionEnfermedadService: ClasificacionEnfermedadService
    ) {
        this.dateAdapter.setLocale('es');
        this.construirFormulario();
    }

    async ngOnInit(): Promise<void> {
        await this.cargarCatalogos();
        console.log('data', this.data);
        if (this.data.informacion) {
            // this.ingresoEnfermedadFichaForm
            //     .get('tipoEnfermdad')
            //     .setValue(this.data.informacion.tokenTipoEnfermedad);
            this.ingresoEnfermedadFichaForm
                .get('detalle')
                .setValue(this.data.informacion.detalle);
            this.ingresoEnfermedadFichaForm
                .get('activo')
                .setValue(this.data.informacion.enfermedadActiva);
            this.ingresoEnfermedadFichaForm
                .get('tratamiento')
                .setValue(this.data.informacion?.tratamiento);
            this.ingresoEnfermedadFichaForm
                .get('edadPresente')
                .setValue(this.data.informacion?.edadPresente);
            if (this.data.informacion.id_temporal) {
                this.ingresoEnfermedadFichaForm
                    .get('id_temporal')
                    .setValue(this.data.informacion.id_temporal);
            }
            this.ingresoEnfermedadFichaForm.patchValue({
                fechaAparicion: new Date(this.data.informacion.fechaAparicion)
            })
            if (this.data.informacion?.fechaAparicion) {
                // const fecha = (this.data.informacion as FichaMedicaEnfermedadDTO).fechaAparicion || new Date();
                // this.ingresoEnfermedadFichaForm.get("fechaAparicion")?.setValue(fecha);

            }
        }
    }

    async cargarCatalogos() {
        // this.funcionesUtils
        //     .obtenerListaCatalogo('TIPO_ENFERMEDAD', '')
        //     .subscribe({
        //         next: (data) => (this.listaTipoEnfermedades = data),
        //         error: (error) =>
        //             console.error(
        //                 'Error cargando grados de instrucción:',
        //                 error
        //             ),
        //     });
        let request: ClasificacionEnfermedadRequest = {
            valor: '',
            sexo: this.data.sexoFicha?.toUpperCase() || ''
        };
    
        if (this.data.informacion && this.data.informacion.clasificacionEnfermedad) {
                request.valor = this.data.informacion.clasificacionEnfermedad.nombre;
        }
    
        this.clasificacionEnfermedadService.obtenerClasificacionEnfermerdades(request, '').subscribe({
            next: (response) => {
            this.listaTipoEnfermedades = response.data;
    
            const controlEnfermedad = this.ingresoEnfermedadFichaForm.get('clasificacionEnfermedad');
    
            if (this.data.informacion && this.data.informacion.clasificacionEnfermedad) {
                const clasificacion = this.listaTipoEnfermedades.find(
                c => c.tokenIdentificador === this.data.informacion.clasificacionEnfermedad.tokenIdentificador
                );
    
                controlEnfermedad
                .setValue(clasificacion);
            }
    
            this.tiposEnfermedadFiltrado = controlEnfermedad.valueChanges.pipe(
                startWith(''),
                debounceTime(300),
                distinctUntilChanged(),
                switchMap((value: string | ClasificacionEnfermedadDTO) => {
                const texto = typeof value === 'string' ? value : value?.nombre;
    
                // request = new ClasificacionEnfermedadRequest();
                request.valor = texto || request.valor;
    
                return this.clasificacionEnfermedadService
                    .obtenerClasificacionEnfermerdades(request, '');
                }),
                map(response => response.data || [])
            );
            },
            error: (error) => console.error('Error cargando grados de instrucción:', error)
        });
    }

    displayFnEnfermedad(option: ClasificacionEnfermedadDTO): string {
        return option && option.codigo && option.nombre
        ? `${option.codigo} | ${option.nombre}`
        : '';
    }

    registrarEnfermedad() {
        if (this.ingresoEnfermedadFichaForm.valid) {
            let enfermedadPersona = new FichaMedicaEnfermedadDTO();
            enfermedadPersona.detalle =
                this.ingresoEnfermedadFichaForm.get('detalle').value;
            enfermedadPersona.enfermedadActiva =
                this.ingresoEnfermedadFichaForm.get('activo').value;
            enfermedadPersona.clasificacionEnfermedad =
                this.ingresoEnfermedadFichaForm.get('clasificacionEnfermedad').value;
            // enfermedadPersona.tokenTipoEnfermedad =
            //     this.ingresoEnfermedadFichaForm.get('tipoEnfermdad').value;
            // enfermedadPersona.nombreEnfermedad =
            //     this.listaTipoEnfermedades.find(
            //         (x) =>
            //             x.tokenIdentificador ==
            //             enfermedadPersona.tokenTipoEnfermedad
            //     ).nombre;
            enfermedadPersona.tratamiento =
                this.ingresoEnfermedadFichaForm.get('tratamiento').value;
            enfermedadPersona.edadPresente =
                this.ingresoEnfermedadFichaForm.get('edadPresente').value;
            enfermedadPersona.fechaAparicion =
                this.ingresoEnfermedadFichaForm.get('fechaAparicion').value;
            if (this.data.informacion) {
                if (this.data.informacion.tokenIdentificador) {
                    enfermedadPersona.tokenIdentificador =
                        this.data.informacion.tokenIdentificador;
                }
                enfermedadPersona.esEdicion = true;
                enfermedadPersona.id_temporal =
                    this.ingresoEnfermedadFichaForm.get('id_temporal').value;
            } else {
                enfermedadPersona.id_temporal = Date.now();
            }
            this.dialogRef.close(enfermedadPersona);
        }
    }

    cerrar() {
        this.dialogRef.close(false);
    }

    soloNumero(event: KeyboardEvent): void {
        const allowedKeys = [
            'Backspace',
            'ArrowLeft',
            'ArrowRight',
            'Tab',
            'Delete',
        ];
        const isNumberKey = event.key >= '0' && event.key <= '9';

        if (!isNumberKey && !allowedKeys.includes(event.key)) {
            event.preventDefault();
        }
    }

    actualizarFecha(event: MatDatepickerInputEvent<Date>, controlName: string) {
        if (event.value) {
            const fecha = event.value;
            this.ingresoEnfermedadFichaForm.get(controlName).setValue(fecha);
        }
    }

    construirFormulario() {
        this.ingresoEnfermedadFichaForm = this.fb.group({
            clasificacionEnfermedad: [null, [autocompleteObjectValidator(), Validators.required]],
            detalle: [null],
            tratamiento: [null],
            edadPresente: [null, [Validators.required]],
            activo: [false, []],
            id_temporal: [],
            fechaAparicion: [null, Validators.required]
        });
    }
}
