import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewEncapsulation,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDrawerToggleResult } from '@angular/material/sidenav';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { UbicacionListComponent } from '../list/list.component';
import { UbicacionBrowserService } from '../services/ubicacion-browser.service';
import { UbicacionNodoItem } from '../ubicacion.types';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';

interface JerarquiaTipoOption {
    jerarquia: JerarquiaDTO;
    level: number;
}

@Component({
    selector: 'ubicacion-details',
    templateUrl: './details.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
        RouterLink,
    ],
})
export class UbicacionDetailsComponent implements OnInit, OnDestroy {
    form = this.fb.group({
        nombre: ['', [Validators.required]],
        nombreCorto: [''],
        descripcion: [''],
        jerarquiaTipo: this.fb.control<JerarquiaDTO | null>(null, [Validators.required]),
    });

    isCreateMode = true;
    parentNode: UbicacionNodoItem | null = null;
    currentItem: UbicacionJerarquiaDTO | null = null;
    jerarquiaTipoOptions: JerarquiaTipoOption[] = [];
    loading = false;

    private _unsubscribeAll = new Subject<void>();

    constructor(
        private fb: FormBuilder,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private changeDetectorRef: ChangeDetectorRef,
        private dialogMensajeService: DialogMensajeService,
        private ubicacionListComponent: UbicacionListComponent,
        private ubicacionBrowserService: UbicacionBrowserService,
        private jerarquiaService: JerarquiaService
    ) {}

    ngOnInit(): void {
        this.ubicacionListComponent.matDrawer.open();

        this.isCreateMode = this.activatedRoute.snapshot.routeConfig?.path === 'details/new';

        if (!this.isCreateMode) {
            this.form.controls.jerarquiaTipo.disable({ emitEvent: false });
        }

        this.loadJerarquiasTipo();

        this.ubicacionBrowserService.level$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((level) => {
                if (!level) {
                    return;
                }

                this.resolveParent(level.entries, level.contextParent || null);
                this.changeDetectorRef.markForCheck();
            });

        if (!this.isCreateMode) {
            this.activatedRoute.data
                .pipe(takeUntil(this._unsubscribeAll))
                .subscribe(({ item }) => {
                    if (!item) {
                        return;
                    }

                    this.currentItem = item as UbicacionJerarquiaDTO;
                    this.form.patchValue({
                        nombre: this.currentItem.nombre || '',
                        nombreCorto: this.currentItem.nombreCorto || '',
                        descripcion: this.currentItem.descripcion || '',
                        jerarquiaTipo: this.currentItem.jerarquiaTipo || null,
                    });
                    this.changeDetectorRef.markForCheck();
                });
        } else {
            this.ubicacionBrowserService.clearSelected();
        }
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

    ngOnDestroy(): void {
        this._unsubscribeAll.next();
        this._unsubscribeAll.complete();
    }

    closeDrawer(): Promise<MatDrawerToggleResult> {
        return this.ubicacionListComponent.matDrawer.close();
    }

    guardar(): void {
        if (this.form.invalid || this.loading) {
            this.form.markAllAsTouched();
            return;
        }

        if (this.isCreateMode && !this.parentNode) {
            this.dialogMensajeService.mensajeAdvertencia('Atencion', 'No se pudo determinar el nodo padre para la nueva ubicacion.');
            return;
        }

        this.loading = true;

        const payload = {
            nombre: (this.form.value.nombre || '').trim(),
            nombreCorto: (this.form.value.nombreCorto || '').trim(),
            descripcion: (this.form.value.descripcion || '').trim(),
            jerarquiaTipo: this.form.getRawValue().jerarquiaTipo || undefined,
        };

        const request$ = this.isCreateMode
            ? this.ubicacionBrowserService.crearUbicacion(payload, this.parentNode)
            : this.ubicacionBrowserService.editarUbicacion(payload, this.currentItem);

        request$
            .pipe(
                switchMap((response) => {
                    if (!response.exito) {
                        throw new Error(response.mensaje || 'No se pudo guardar la ubicacion');
                    }

                    this.dialogMensajeService.mensajeExitoso(response.titulo || 'Exito', response.mensaje || 'Operacion realizada correctamente');
                    return this.ubicacionBrowserService.refreshLevel(this.activatedRoute.parent.snapshot);
                }),
                takeUntil(this._unsubscribeAll)
            )
            .subscribe({
                next: () => {
                    this.loading = false;
                    this.router.navigate(['../..'], { relativeTo: this.activatedRoute });
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
            .mensajeConConfirmacion('Esta accion eliminara la ubicacion seleccionada.', 'Desea continuar?')
            .afterClosed()
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((resp: 'confirmed' | 'cancelled') => {
                if (resp !== 'confirmed') {
                    return;
                }

                this.loading = true;

                this.ubicacionBrowserService
                    .eliminarUbicacion(this.currentItem)
                    .pipe(
                        switchMap((response) => {
                            if (!response.exito) {
                                throw new Error(response.mensaje || 'No se pudo eliminar la ubicacion');
                            }

                            this.dialogMensajeService.mensajeExitoso(response.titulo || 'Exito', response.mensaje || 'Ubicación eliminada correctamente');
                            return this.ubicacionBrowserService.refreshLevel(this.activatedRoute.parent.snapshot);
                        }),
                        takeUntil(this._unsubscribeAll)
                    )
                    .subscribe({
                        next: () => {
                            this.loading = false;
                            this.router.navigate(['../..'], { relativeTo: this.activatedRoute });
                        },
                        error: (error) => {
                            this.loading = false;
                            this.ubicacionBrowserService.showError(error);
                            this.changeDetectorRef.markForCheck();
                        },
                    });
            });
    }

    private resolveParent(entries: UbicacionNodoItem[], contextParent: UbicacionNodoItem | null): void {
        const parentId = this.activatedRoute.snapshot.queryParamMap.get('parentId');
        const parentType = this.activatedRoute.snapshot.queryParamMap.get('parentType') as
            | 'centro'
            | 'ubicacion'
            | null;

        if (!parentId || !parentType) {
            this.parentNode = contextParent;
            return;
        }

        if (contextParent && contextParent.id === parentId && contextParent.nodeType === parentType) {
            this.parentNode = contextParent;
            return;
        }

        const found = entries.find((entry) => entry.id === parentId && entry.nodeType === parentType) || null;
        this.parentNode = found || contextParent;
    }

    private getJerarquiaLevel(target: JerarquiaDTO, jerarquias: JerarquiaDTO[], currentLevel: number = 0): number | null {
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

                    // Determine minLevel based on parent node type and jerarquiaTipo
                    let minLevel: number | undefined = undefined;
                    
                    if (this.parentNode) {
                        if (this.parentNode.nodeType === 'centro') {
                            // Centro: allow all hierarchy levels
                            minLevel = undefined;
                        } else if (this.parentNode.nodeType === 'ubicacion') {
                            // Ubicacion: get its jerarquiaTipo and allow only deeper levels
                            const parentJerarquiaTipo = (this.parentNode.raw as UbicacionJerarquiaDTO).jerarquiaTipo;
                            if (parentJerarquiaTipo) {
                                const parentLevel = this.getJerarquiaLevel(parentJerarquiaTipo, response.data || []);
                                if (parentLevel !== null) {
                                    minLevel = parentLevel + 1;
                                }
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
            // Only include if currentLevel >= minLevel (or minLevel is undefined)
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
