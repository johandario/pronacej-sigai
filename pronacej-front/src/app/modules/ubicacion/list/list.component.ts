import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDrawer, MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
    ActivatedRoute,
    Router,
    RouterLink,
    RouterOutlet,
} from '@angular/router';
import { FuseMediaWatcherService } from '@fuse/services/media-watcher';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { UbicacionBrowserService } from '../services/ubicacion-browser.service';
import {
    UbicacionLevelData,
    UbicacionNodoItem,
} from '../ubicacion.types';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';
import {
    UbicacionDetailsModalComponent,
    UbicacionDetailsModalData,
    UbicacionDetailsModalResult,
} from '../modal/ubicacion-details-modal.component';

@Component({
    selector: 'ubicacion-list',
    templateUrl: './list.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        MatSidenavModule,
        RouterOutlet,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatTooltipModule,
    ],
})
export class UbicacionListComponent implements OnInit, OnDestroy {
    @ViewChild('matDrawer', { static: true }) matDrawer: MatDrawer;

    drawerMode: 'side' | 'over' = 'side';
    level: UbicacionLevelData;

    private _unsubscribeAll = new Subject<void>();

    constructor(
        private activatedRoute: ActivatedRoute,
        private changeDetectorRef: ChangeDetectorRef,
        private router: Router,
        private matDialog: MatDialog,
        private ubicacionBrowserService: UbicacionBrowserService,
        private fuseMediaWatcherService: FuseMediaWatcherService,
        private fuseConfirmationService: FuseConfirmationService
    ) {}

    ngOnInit(): void {
        this.activatedRoute.data
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(({ level }) => {
                if (level) {
                    this.level = level as UbicacionLevelData;
                    this.changeDetectorRef.markForCheck();
                }
            });

        this.ubicacionBrowserService.level$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((level) => {
                if (level) {
                    this.level = level;
                    this.changeDetectorRef.markForCheck();
                }
            });

        this.fuseMediaWatcherService
            .onMediaQueryChange$('(min-width: 1440px)')
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((state) => {
                this.drawerMode = state.matches ? 'side' : 'over';
                this.changeDetectorRef.markForCheck();
            });
    }

    ngOnDestroy(): void {
        this._unsubscribeAll.next();
        this._unsubscribeAll.complete();
    }

    onBackdropClicked(): void {
        this.router.navigate(['./'], { relativeTo: this.activatedRoute });
        this.changeDetectorRef.markForCheck();
    }

    abrirNivel(item: UbicacionNodoItem): void {
        if (item.isLeaf) {
            return;
        }

        if (item.nodeType === 'centro') {
            this.router.navigate(['/configuracion/ubicacion/centros', item.id]);
            return;
        }

        this.router.navigate(['/configuracion/ubicacion/ubicaciones', item.id]);
    }

    abrirCreacionHija(item: UbicacionNodoItem): void {
        if (item.isLeaf) {
            return;
        }

        this.router.navigate(['./details/new'], {
            relativeTo: this.activatedRoute,
            queryParams: {
                parentType: item.nodeType,
                parentId: item.id,
            },
        });
    }

    abrirCreacionHijaModal(item: UbicacionNodoItem): void {
        if (item.isLeaf) {
            return;
        }

        this.openUbicacionModal({
            isCreateMode: true,
            parentNode: item,
        });
    }

    abrirEdicion(item: UbicacionNodoItem): void {
        if (item.nodeType === 'centro') {
            return;
        }

        this.router.navigate(['./details', item.id], {
            relativeTo: this.activatedRoute,
        });
    }

    abrirEdicionModal(item: UbicacionNodoItem): void {
        if (item.nodeType === 'centro') {
            return;
        }

        this.openUbicacionModal({
            isCreateMode: false,
            currentItem: item.raw as UbicacionJerarquiaDTO,
        });
    }

    crearHijaEnContexto(): void {
        if (!this.level?.contextParent || this.level.contextParent.isLeaf) {
            return;
        }

        this.abrirCreacionHija(this.level.contextParent);
    }

    crearHijaEnContextoModal(): void {
        if (!this.level?.contextParent || this.level.contextParent.isLeaf) {
            return;
        }

        this.abrirCreacionHijaModal(this.level.contextParent);
    }

    irBreadcrumb(item: { id: string; nodeType: 'centro' | 'ubicacion' }): void {
        if (item.nodeType === 'centro') {
            this.router.navigate(['/configuracion/ubicacion/centros', item.id]);
            return;
        }

        this.router.navigate(['/configuracion/ubicacion/ubicaciones', item.id]);
    }

    trackByFn(index: number, item: UbicacionNodoItem): string | number {
        return item?.id || index;
    }

    eliminarUbicacion(item: UbicacionNodoItem): void {
        const dialogRef = this.fuseConfirmationService.open({
            title: 'Eliminar ubicación',
            message: `¿Estás seguro de que deseas eliminar la ubicación "${item.name}"? Esta acción eliminará también toda su jerarquía de hijas.`,
            actions: {
                confirm: {
                    label: 'Eliminar',
                    color: 'warn',
                },
                cancel: {
                    label: 'Cancelar',
                },
            },
        });

        dialogRef.afterClosed().pipe(takeUntil(this._unsubscribeAll)).subscribe((result) => {
            if (result === 'confirmed') {
                const ubicacionDto = item.raw as UbicacionJerarquiaDTO;
                this.ubicacionBrowserService
                    .eliminarUbicacion(ubicacionDto)
                    .pipe(
                        switchMap((response) => {
                            if (!response.exito) {
                                throw new Error(response.mensaje || 'No se pudo eliminar la ubicación');
                            }

                            return this.ubicacionBrowserService.refreshLevel(
                                this.activatedRoute.snapshot
                            );
                        }),
                        takeUntil(this._unsubscribeAll)
                    )
                    .subscribe({
                        next: () => {},
                        error: (error) => {
                            console.error('Error al eliminar ubicación:', error);
                        },
                    });
            }
        });
    }

    private openUbicacionModal(data: UbicacionDetailsModalData): void {
        this.matDialog
            .open<UbicacionDetailsModalComponent, UbicacionDetailsModalData, UbicacionDetailsModalResult>(
                UbicacionDetailsModalComponent,
                {
                    width: '720px',
                    maxWidth: '95vw',
                    disableClose: true,
                    data,
                }
            )
            .afterClosed()
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((result) => {
                if (!result?.saved) {
                    return;
                }

                this.ubicacionBrowserService
                    .refreshLevel(this.activatedRoute.snapshot)
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe({
                        next: () => {
                            this.changeDetectorRef.markForCheck();
                        },
                        error: (error) => {
                            this.ubicacionBrowserService.showError(error);
                            this.changeDetectorRef.markForCheck();
                        },
                    });
            });
    }
}
