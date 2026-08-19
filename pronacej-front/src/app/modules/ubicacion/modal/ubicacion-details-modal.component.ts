import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Inject,
    OnDestroy,
    OnInit,
    ViewEncapsulation,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { Observable, Subject, catchError, forkJoin, takeUntil, tap, throwError } from 'rxjs';
import { UbicacionBrowserService } from '../services/ubicacion-browser.service';
import { UbicacionNodoItem } from '../ubicacion.types';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';

interface JerarquiaTipoOption {
    jerarquia: JerarquiaDTO;
    level: number;
}

const NEMONICA_UBICACION_TIPO_ETAPA = 'UBICACION_TIPO_ETAPA';
const NEMONICA_UBICACION_TIPO_CELDA = 'UBICACION_TIPO_CELDA';

export interface UbicacionDetailsModalData {
    isCreateMode: boolean;
    parentNode?: UbicacionNodoItem | null;
    currentItem?: UbicacionJerarquiaDTO | null;
}

export interface UbicacionDetailsModalResult {
    saved: boolean;
    action: 'create' | 'edit' | 'delete';
}

@Component({
    selector: 'ubicacion-details-modal',
    templateUrl: './ubicacion-details-modal.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatDialogModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
    ],
})
export class UbicacionDetailsModalComponent implements OnInit, OnDestroy {
    form = this.fb.group({
        nombre: ['', [Validators.required]],
        nombreCorto: [''],
        descripcion: [''],
        jerarquiaTipo: this.fb.control<JerarquiaDTO | null>(null, [Validators.required]),
        tipoSexo: this.fb.control<CatalogoDTO | null>(null),
        atencionPrioritaria: this.fb.control<CatalogoDTO | null>(null),
        tipoUbicacion: this.fb.control<CatalogoDTO | null>(null),
        rangoInicio: this.fb.control<number | null>(null),
        rangoFin: this.fb.control<number | null>(null),
    });

    isCreateMode = true;
    parentNode: UbicacionNodoItem | null = null;
    currentItem: UbicacionJerarquiaDTO | null = null;
    jerarquiaTipoOptions: JerarquiaTipoOption[] = [];
    loading = false;
    cantidadCamas: number | null = null;

    tipoSexoOptions: CatalogoDTO[] = [];
    atencionPrioritariaOptions: CatalogoDTO[] = []
    tipoUbicacionOptions: CatalogoDTO[] = [];

    private _unsubscribeAll = new Subject<void>();

    constructor(
        private fb: FormBuilder,
        private changeDetectorRef: ChangeDetectorRef,
        private dialogMensajeService: DialogMensajeService,
        private ubicacionBrowserService: UbicacionBrowserService,
        private jerarquiaService: JerarquiaService,
        private catalogoService: CatalogoService,
        private dialogRef: MatDialogRef<UbicacionDetailsModalComponent, UbicacionDetailsModalResult>,
        @Inject(MAT_DIALOG_DATA) private data: UbicacionDetailsModalData
    ) {}

    ngOnInit(): void {
        this.isCreateMode = this.data?.isCreateMode ?? true;
        this.parentNode = this.data?.parentNode || null;
        this.currentItem = this.data?.currentItem || null;

        if (!this.isCreateMode) {
            this.form.controls.jerarquiaTipo.disable({ emitEvent: false });
        }

        this.loadJerarquiasTipo();
        this.loadCatalogos();
        this.setupJerarquiaTipoListener();

        if (this.currentItem) {
            this.form.patchValue({
                nombre: this.currentItem.nombre || '',
                nombreCorto: this.currentItem.nombreCorto || '',
                descripcion: this.currentItem.descripcion || '',
                jerarquiaTipo: this.currentItem.jerarquiaTipo || null,
                tipoSexo: this.currentItem.tipoSexo || null,
                atencionPrioritaria: this.currentItem.atencionPrioritaria || null,
                tipoUbicacion: this.currentItem.tipoUbicacion || null,
                rangoInicio: this.currentItem.rangoInicio || null,
                rangoFin: this.currentItem.rangoFin || null,
            });

            this.calculateCantidadCamas();
            this.updateValidators();
        }
    }

    private setupJerarquiaTipoListener(): void {
        this.form.controls.jerarquiaTipo.valueChanges
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(() => {
                this.updateValidators();
                this.cleanupFieldsForType();
                this.changeDetectorRef.markForCheck();
            });

        this.form.controls.rangoInicio.valueChanges
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(() => {
                this.calculateCantidadCamas();
                this.form.controls.rangoFin.updateValueAndValidity({ emitEvent: false });
                this.changeDetectorRef.markForCheck();
            });

        this.form.controls.rangoFin.valueChanges
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(() => {
                this.calculateCantidadCamas();
                this.changeDetectorRef.markForCheck();
            });
    }

    compareJerarquiaByToken = (
        first: JerarquiaDTO | null,
        second: JerarquiaDTO | null
    ): boolean => {
        if (!first || !second) {
            return first === second;
        }

        return first.tokenIdentificador === second.tokenIdentificador;
    };

    getNemonico(): string {
        const jerarquiaTipo = this.form.controls.jerarquiaTipo.value;
        return jerarquiaTipo?.nemonico || '';
    }

    getIsEtapa(): boolean {
        return this.getNemonico() === NEMONICA_UBICACION_TIPO_ETAPA;
    }

    getIsCelda(): boolean {
        return this.getNemonico() === NEMONICA_UBICACION_TIPO_CELDA;
    }

    hasJerarquiaTipo(): boolean {
        return !!this.form.controls.jerarquiaTipo.value;
    }

    private updateValidators(): void {
        const isEtapa = this.getIsEtapa();
        const isCelda = this.getIsCelda();
        const hasType = this.hasJerarquiaTipo();

        const tipoSexoControl = this.form.controls.tipoSexo;
        const atencionPrioritariaControl = this.form.controls.atencionPrioritaria;
        const tipoUbicacionControl = this.form.controls.tipoUbicacion;
        const rangoInicioControl = this.form.controls.rangoInicio;
        const rangoFinControl = this.form.controls.rangoFin;

        tipoSexoControl.clearValidators();
        atencionPrioritariaControl.clearValidators();
        tipoUbicacionControl.clearValidators();
        rangoInicioControl.clearValidators();
        rangoFinControl.clearValidators();
        this.form.clearValidators();

        if (hasType) {
            atencionPrioritariaControl.setValidators([Validators.required]);
            tipoUbicacionControl.setValidators([Validators.required]);

            if (!isEtapa) {
                tipoSexoControl.setValidators([Validators.required]);
            }
        }

        if (isCelda) {
            rangoInicioControl.setValidators([Validators.required, Validators.min(0)]);
            rangoFinControl.setValidators([
                Validators.required,
                Validators.min(0),
                (control) => {
                    const fin = control.value;
                    const inicio = control.parent?.get('rangoInicio')?.value;
                    if (fin !== null && inicio !== null &&
                        typeof fin === 'number' && typeof inicio === 'number' &&
                        fin <= inicio) {
                        return { rangoInvalid: true };
                    }
                    return null;
                },
            ]);
        }

        tipoSexoControl.updateValueAndValidity({ emitEvent: false });
        atencionPrioritariaControl.updateValueAndValidity({ emitEvent: false });
        tipoUbicacionControl.updateValueAndValidity({ emitEvent: false });
        rangoInicioControl.updateValueAndValidity({ emitEvent: false });
        rangoFinControl.updateValueAndValidity({ emitEvent: false });

        this.form.updateValueAndValidity({ emitEvent: false });
    }

    private cleanupFieldsForType(): void {
        if (!this.isCreateMode) {
            return;
        }

        const isEtapa = this.getIsEtapa();
        const isCelda = this.getIsCelda();

        if (isEtapa) {
            this.form.controls.tipoSexo.reset(null, { emitEvent: false });
            this.form.controls.rangoInicio.reset(null, { emitEvent: false });
            this.form.controls.rangoFin.reset(null, { emitEvent: false });
            this.cantidadCamas = null;
        } else if (isCelda) {
            this.form.controls.tipoSexo.reset(null, { emitEvent: false });
            this.form.controls.atencionPrioritaria.reset(null, { emitEvent: false });
            this.form.controls.tipoUbicacion.reset(null, { emitEvent: false });
        } else {
            this.form.controls.rangoInicio.reset(null, { emitEvent: false });
            this.form.controls.rangoFin.reset(null, { emitEvent: false });
            this.cantidadCamas = null;
        }
    }

    private calculateCantidadCamas(): void {
        const rangoInicio = this.form.controls.rangoInicio.value;
        const rangoFin = this.form.controls.rangoFin.value;

        if (this.getIsCelda() && rangoInicio !== null && rangoFin !== null &&
            typeof rangoInicio === 'number' && typeof rangoFin === 'number') {
            if (rangoFin > rangoInicio) {
                this.cantidadCamas = rangoFin - rangoInicio + 1;
            } else {
                this.cantidadCamas = null;
            }
        } else {
            this.cantidadCamas = null;
        }
    }

    ngOnDestroy(): void {
        this._unsubscribeAll.next();
        this._unsubscribeAll.complete();
    }

    cancelar(): void {
        this.dialogRef.close();
    }

    guardar(): void {
        if (this.form.invalid || this.loading) {
            this.form.markAllAsTouched();
            return;
        }

        if (this.isCreateMode && !this.parentNode) {
            this.dialogMensajeService.mensajeAdvertencia(
                'Atencion',
                'No se pudo determinar el nodo padre para la nueva ubicacion.'
            );
            return;
        }

        this.loading = true;

        const rawValue = this.form.getRawValue();
        const payload: any = {
            nombre: (this.form.value.nombre || '').trim(),
            nombreCorto: (this.form.value.nombreCorto || '').trim(),
            descripcion: (this.form.value.descripcion || '').trim(),
            jerarquiaTipo: rawValue.jerarquiaTipo || undefined,
            tipoSexo: rawValue.tipoSexo || undefined,
            atencionPrioritaria: rawValue.atencionPrioritaria || undefined,
            tipoUbicacion: rawValue.tipoUbicacion || undefined,
        };

        // Incluir rango si es CELDA
        if (this.getIsCelda()) {
            payload.rangoInicio = rawValue.rangoInicio;
            payload.rangoFin = rawValue.rangoFin;
        }

        const request$ = this.isCreateMode
            ? this.ubicacionBrowserService.crearUbicacion(payload, this.parentNode)
            : this.ubicacionBrowserService.editarUbicacion(payload, this.currentItem);

        request$.pipe(takeUntil(this._unsubscribeAll)).subscribe({
            next: (response) => {
                this.loading = false;

                if (!response.exito) {
                    this.dialogMensajeService.mensajeAdvertencia(
                        response.titulo || 'Atencion',
                        response.mensaje || 'No se pudo guardar la ubicacion.'
                    );
                    this.changeDetectorRef.markForCheck();
                    return;
                }

                this.dialogMensajeService.mensajeExitoso(
                    response.titulo || 'Exito',
                    response.mensaje || 'Operacion realizada correctamente'
                );

                this.dialogRef.close({
                    saved: true,
                    action: this.isCreateMode ? 'create' : 'edit',
                });
            },
            error: (error) => {
                this.loading = false;
                this.ubicacionBrowserService.showError(error);
                this.changeDetectorRef.markForCheck();
            },
        });
    }

    eliminar(): void {
        if (this.isCreateMode || !this.currentItem || this.loading) {
            return;
        }

        this.dialogMensajeService
            .mensajeConConfirmacion(
                'Esta accion eliminara la ubicacion seleccionada.',
                'Desea continuar?'
            )
            .afterClosed()
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((resp: 'confirmed' | 'cancelled') => {
                if (resp !== 'confirmed') {
                    return;
                }

                this.loading = true;

                this.ubicacionBrowserService
                    .eliminarUbicacion(this.currentItem)
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe({
                        next: (response) => {
                            this.loading = false;

                            if (!response.exito) {
                                this.dialogMensajeService.mensajeAdvertencia(
                                    response.titulo || 'Atencion',
                                    response.mensaje || 'No se pudo eliminar la ubicacion.'
                                );
                                this.changeDetectorRef.markForCheck();
                                return;
                            }

                            this.dialogMensajeService.mensajeExitoso(
                                response.titulo || 'Exito',
                                response.mensaje || 'Ubicación eliminada correctamente'
                            );

                            this.dialogRef.close({
                                saved: true,
                                action: 'delete',
                            });
                        },
                        error: (error) => {
                            this.loading = false;
                            this.ubicacionBrowserService.showError(error);
                            this.changeDetectorRef.markForCheck();
                        },
                    });
            });
    }

    private getJerarquiaLevel(
        target: JerarquiaDTO,
        jerarquias: JerarquiaDTO[],
        currentLevel: number = 0
    ): number | null {
        for (const jerarquia of jerarquias) {
            if (jerarquia.tokenIdentificador === target.tokenIdentificador) {
                return currentLevel;
            }

            const hijos = jerarquia.hijos || [];
            if (hijos.length > 0) {
                const foundLevel = this.getJerarquiaLevel(target, hijos, currentLevel + 1);
                if (foundLevel !== null) {
                    return foundLevel;
                }
            }
        }

        return null;
    }

    private loadCatalogos(): void {
        this.obtenerCatalogos()
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe({
                next: () => {
                    this.changeDetectorRef.markForCheck();
                },
                error: () => {
                    this.changeDetectorRef.markForCheck();
                },
            });
    }

    private obtenerCatalogos() : Observable<any> {
        const nemonicosCatalogos = [
            'TIPO_SEXO',        
            'UBICACION_ATENCION_PRIORITARIA',        
            'UBICACION_TIPO_UBICACION'        
        ];

        const solicitudes = nemonicosCatalogos.map(solicitud => this.catalogoService.obtenerHijos(solicitud, ''));
        
        return forkJoin(solicitudes).pipe(
            tap((results: any[]) => {
                this.tipoSexoOptions = results[0]?.data;                 
                this.atencionPrioritariaOptions = results[1]?.data;
                this.tipoUbicacionOptions = results[2]?.data;
            }),
            catchError(err => {
                this.catalogoService.checkError(err);
                return throwError(() => err); 
            })
        );
    }

    private loadJerarquiasTipo(): void {
        this.jerarquiaService
            .obtenerJerarquiasPorNemonicoPadreCompleto(
                etiquetasModel.NEMONICO_JERARQUIA_PADRE_UBICACION_TIPOS,
                ''
            )
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe({
                next: (response) => {
                    if (!response.exito) {
                        this.dialogMensajeService.mensajeAdvertencia(
                            response.titulo || 'Atencion',
                            response.mensaje || 'No se pudieron cargar los tipos de ubicacion.'
                        );
                        this.jerarquiaTipoOptions = [];
                        this.changeDetectorRef.markForCheck();
                        return;
                    }

                    let minLevel: number | undefined;

                    if (this.parentNode?.nodeType === 'ubicacion') {
                        const parentJerarquiaTipo =
                            (this.parentNode.raw as UbicacionJerarquiaDTO)?.jerarquiaTipo;

                        if (parentJerarquiaTipo) {
                            const parentLevel = this.getJerarquiaLevel(
                                parentJerarquiaTipo,
                                response.data || []
                            );
                            if (parentLevel !== null) {
                                minLevel = parentLevel + 1;
                            }
                        }
                    }

                    this.jerarquiaTipoOptions = this.flattenJerarquias(
                        response.data || [],
                        minLevel
                    );

                    if (!this.isCreateMode && this.currentItem?.jerarquiaTipo) {
                        const jerarquiaSeleccionada = this.jerarquiaTipoOptions.find(
                            (option) =>
                                option.jerarquia.tokenIdentificador ===
                                this.currentItem.jerarquiaTipo.tokenIdentificador
                        );

                        if (jerarquiaSeleccionada) {
                            this.form.patchValue({
                                jerarquiaTipo: jerarquiaSeleccionada.jerarquia,
                            });
                        }
                    }

                    this.changeDetectorRef.markForCheck();
                },
                error: (error) => {
                    this.jerarquiaTipoOptions = [];
                    this.ubicacionBrowserService.showError(error);
                    this.changeDetectorRef.markForCheck();
                },
            });
    }

    private flattenJerarquias(
        jerarquias: JerarquiaDTO[],
        minLevel: number | undefined = undefined,
        currentLevel: number = 0
    ): JerarquiaTipoOption[] {
        const options: JerarquiaTipoOption[] = [];

        for (const jerarquia of jerarquias) {
            if (minLevel === undefined || currentLevel >= minLevel) {
                options.push({
                    jerarquia,
                    level: currentLevel,
                });
            }

            const hijos = jerarquia.hijos || [];
            if (hijos.length) {
                options.push(...this.flattenJerarquias(hijos, minLevel, currentLevel + 1));
            }
        }

        return options;
    }
}
