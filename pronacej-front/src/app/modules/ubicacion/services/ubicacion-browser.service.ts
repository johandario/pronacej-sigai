import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import etiquetasModel from 'app/core/etiquetas.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { UbicacionJerarquiaDTO } from 'app/core/model/both/ubicacionJerarquiaDTO.model';
import { JerarquiasPorNemonicosPadreRequest } from 'app/core/model/request/JerarquiasPorNemonicosPadreRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { JerarquiaService } from 'app/modules/seguridad/services/jerarquia.service';
import { BehaviorSubject, Observable, forkJoin, map, switchMap, of } from 'rxjs';
import { UbicacionJerarquiaService } from './ubicacionJerarquia.service';
import {
    UBICACION_TIPO_CELDA,
    UbicacionBreadcrumb,
    UbicacionLevelData,
    UbicacionNodoItem,
    UbicacionSavePayload,
} from '../ubicacion.types';

@Injectable({ providedIn: 'root' })
export class UbicacionBrowserService {
    private _level = new BehaviorSubject<UbicacionLevelData | null>(null);
    private _selectedItem = new BehaviorSubject<UbicacionJerarquiaDTO | null>(null);

    readonly level$ = this._level.asObservable();
    readonly selectedItem$ = this._selectedItem.asObservable();

    constructor(
        private ubicacionJerarquiaService: UbicacionJerarquiaService,
        private jerarquiaService: JerarquiaService,
        private dialogMensajeService: DialogMensajeService
    ) {}

    loadLevel(route: ActivatedRouteSnapshot, nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACION): Observable<UbicacionLevelData> {
        const centroId = route.paramMap.get('centroId');
        const ubicacionId = route.paramMap.get('ubicacionId');

        if (ubicacionId) {
            return this.loadByUbicacion(ubicacionId, nemonicoMenu);
        }

        if (centroId) {
            return this.loadByCentro(centroId, nemonicoMenu);
        }

        return this.loadCentros(nemonicoMenu);
    }

    refreshLevel(route: ActivatedRouteSnapshot, nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACION): Observable<UbicacionLevelData> {
        return this.loadLevel(route, nemonicoMenu);
    }

    getUbicacionById(id: string, nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACION): Observable<UbicacionJerarquiaDTO> {
        return this.ubicacionJerarquiaService
            .obtenerPorTokenIdentificador(id, nemonicoMenu)
            .pipe(
                map((response: RespuestaPorDefecto<UbicacionJerarquiaDTO>) => {
                    if (!response.exito) {
                        throw new Error(response.mensaje || 'No se pudo obtener la ubicacion');
                    }
                    this._selectedItem.next(response.data);
                    return response.data;
                })
            );
    }

    clearSelected(): void {
        this._selectedItem.next(null);
    }

    crearUbicacion(
        payload: UbicacionSavePayload,
        parent: UbicacionNodoItem,
        nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACION
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO>> {
        const dto = new UbicacionJerarquiaDTO();
        dto.nombre = payload.nombre;
        dto.nombreCorto = payload.nombreCorto;
        dto.descripcion = payload.descripcion;
        dto.jerarquiaTipo = payload.jerarquiaTipo;
        dto.tipoSexo = payload.tipoSexo;
        dto.atencionPrioritaria = payload.atencionPrioritaria;
        dto.tipoUbicacion = payload.tipoUbicacion;
        dto.rangoInicio = payload.rangoInicio;
        dto.rangoFin = payload.rangoFin;
        dto.esEdicion = true;

        if (parent.nodeType === 'centro') {
            // When creating a child under a centro, only set jerarquiaCentro
            // ubicacionJerarquiaPadre should be null/empty for root ubicaciones
            const centro = parent.raw as JerarquiaDTO;
            dto.jerarquiaCentro = { tokenIdentificador: centro.tokenIdentificador } as JerarquiaDTO;
            // DO NOT set ubicacionJerarquiaPadre for root ubicaciones
        } else {
            // When creating a child under an ubicacion, set both jerarquiaCentro and ubicacionJerarquiaPadre
            const ubicacionPadre = parent.raw as UbicacionJerarquiaDTO;
            dto.jerarquiaCentro = ubicacionPadre.jerarquiaCentro;
            dto.ubicacionJerarquiaPadre = { tokenIdentificador: ubicacionPadre.tokenIdentificador } as UbicacionJerarquiaDTO;
        }

        return this.ubicacionJerarquiaService.crearEditar(dto, nemonicoMenu);
    }

    editarUbicacion(
        payload: UbicacionSavePayload,
        item: UbicacionJerarquiaDTO,
        nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACION
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO>> {
        const dto = { ...item } as UbicacionJerarquiaDTO;
        dto.nombre = payload.nombre;
        dto.nombreCorto = payload.nombreCorto;
        dto.descripcion = payload.descripcion;
        dto.tipoSexo = payload.tipoSexo;
        dto.atencionPrioritaria = payload.atencionPrioritaria;
        dto.tipoUbicacion = payload.tipoUbicacion;
        dto.rangoInicio = payload.rangoInicio;
        dto.rangoFin = payload.rangoFin;
        dto.esEdicion = true;

        return this.ubicacionJerarquiaService.crearEditar(dto, nemonicoMenu);
    }

    eliminarUbicacion(
        item: UbicacionJerarquiaDTO,
        nemonicoMenu: string = etiquetasModel.NEMONICO_MENU_UBICACION
    ): Observable<RespuestaPorDefecto<UbicacionJerarquiaDTO>> {
        return this.ubicacionJerarquiaService.eliminar(item, nemonicoMenu);
    }

    private loadCentros(nemonicoMenu: string): Observable<UbicacionLevelData> {
        const request = new JerarquiasPorNemonicosPadreRequest();
        request.nemonicosPadre = ['CJDR'];

        return this.jerarquiaService
            .obtenerJerarquiasPorNemonicoPadreLista(request, nemonicoMenu)
            .pipe(
                map(
                    (
                        response: RespuestaPorDefecto<
                            Record<string, JerarquiaDTO[]>
                        >
                    ) => {
                    if (!response.exito) {
                        throw new Error(response.mensaje || 'No se pudieron cargar los centros');
                    }

                    const centros = Object.values(response.data || {}).flat();
                    const entries = centros.map((centro) => this.mapCentroToEntry(centro));

                    const level: UbicacionLevelData = {
                        title: 'Centros',
                        entries,
                        breadcrumbs: [],
                    };

                    this._level.next(level);
                    return level;
                    }
                )
            );
    }

    private loadByCentro(centroId: string, nemonicoMenu: string): Observable<UbicacionLevelData> {
        const request = new JerarquiasPorNemonicosPadreRequest();
        request.nemonicosPadre = ['CJDR'];

        return forkJoin({
            centrosResponse: this.jerarquiaService.obtenerJerarquiasPorNemonicoPadreLista(request, nemonicoMenu),
            // Fetch all ubicaciones under this centro (top-level, no intermediate parent)
            hijosResponse: this.ubicacionJerarquiaService.obtenerPorTokenIdentificadorJerarquiaCentro(centroId, nemonicoMenu),
        }).pipe(
            map(({ centrosResponse, hijosResponse }) => {
                if (!centrosResponse.exito) {
                    throw new Error(centrosResponse.mensaje || 'No se pudo cargar el centro');
                }
                if (!hijosResponse.exito) {
                    throw new Error(hijosResponse.mensaje || 'No se pudieron cargar las ubicaciones');
                }

                const centros = Object.values(centrosResponse.data || {}).flat();
                const centro = centros.find((it) => it.tokenIdentificador === centroId);
                if (!centro) {
                    throw new Error('Centro no encontrado');
                }

                // Only show root ubicaciones (those without an ubicacionJerarquiaPadre)
                const entries = (hijosResponse.data || [])
                    .filter((ubicacion) => !ubicacion.ubicacionJerarquiaPadre?.tokenIdentificador)
                    .map((ubicacion) => this.mapUbicacionToEntry(ubicacion));
                const centroEntry = this.mapCentroToEntry(centro);

                const level: UbicacionLevelData = {
                    title: centro.nombre,
                    entries,
                    breadcrumbs: [
                        {
                            id: centro.tokenIdentificador,
                            name: centro.nombre,
                            nodeType: 'centro',
                        },
                    ],
                    contextParent: centroEntry,
                };

                this._level.next(level);
                return level;
            })
        );
    }

    private loadByUbicacion(ubicacionId: string, nemonicoMenu: string): Observable<UbicacionLevelData> {
        return this.getUbicacionById(ubicacionId, nemonicoMenu).pipe(
            switchMap((ubicacion: UbicacionJerarquiaDTO) => {
                return this.ubicacionJerarquiaService
                    .obtenerHijosPorTokenIdentificadorPadre(ubicacionId, nemonicoMenu)
                    .pipe(
                        switchMap((hijosResponse: RespuestaPorDefecto<UbicacionJerarquiaDTO[]>) => {
                            if (!hijosResponse.exito) {
                                throw new Error(hijosResponse.mensaje || 'No se pudieron cargar los hijos');
                            }

                            // Build breadcrumbs asynchronously to get complete ancestor chain
                            return this.buildBreadcrumbsAsync(ubicacion).pipe(
                                map((breadcrumbs) => {
                                    const entries = (hijosResponse.data || []).map((hijo) =>
                                        this.mapUbicacionToEntry(hijo)
                                    );
                                    const parentEntry = this.mapUbicacionToEntry(ubicacion);

                                    const level: UbicacionLevelData = {
                                        title: ubicacion.nombre,
                                        entries,
                                        breadcrumbs,
                                        contextParent: parentEntry,
                                    };

                                    this._level.next(level);
                                    return level;
                                })
                            );
                        })
                    );
            })
        );
    }

    private buildBreadcrumbs(ubicacion: UbicacionJerarquiaDTO): UbicacionBreadcrumb[] {
        const breadcrumbs: UbicacionBreadcrumb[] = [];

        if (ubicacion.jerarquiaCentro?.tokenIdentificador) {
            breadcrumbs.push({
                id: ubicacion.jerarquiaCentro.tokenIdentificador,
                name: ubicacion.jerarquiaCentro.nombre || 'Centro',
                nodeType: 'centro',
            });
        }

        breadcrumbs.push({
            id: ubicacion.tokenIdentificador,
            name: ubicacion.nombre,
            nodeType: 'ubicacion',
        });

        return breadcrumbs;
    }

    private buildBreadcrumbsAsync(ubicacion: UbicacionJerarquiaDTO): Observable<UbicacionBreadcrumb[]> {
        const cachedBreadcrumbs = this.buildBreadcrumbsFromCurrentLevel(ubicacion);
        if (cachedBreadcrumbs) {
            return of(cachedBreadcrumbs);
        }

        const breadcrumbs: UbicacionBreadcrumb[] = [];

        // Start with the centro
        if (ubicacion.jerarquiaCentro?.tokenIdentificador) {
            breadcrumbs.push({
                id: ubicacion.jerarquiaCentro.tokenIdentificador,
                name: ubicacion.jerarquiaCentro.nombre || 'Centro',
                nodeType: 'centro',
            });
        }

        // Build the full ancestor chain recursively
        return this.buildAncestorChain(ubicacion).pipe(
            map((ancestors) => {
                breadcrumbs.push(...ancestors);
                return breadcrumbs;
            })
        );
    }

    private buildBreadcrumbsFromCurrentLevel(
        ubicacion: UbicacionJerarquiaDTO
    ): UbicacionBreadcrumb[] | null {
        const currentLevel = this._level.getValue();

        if (!currentLevel?.entries?.some((entry) => entry.id === ubicacion.tokenIdentificador)) {
            return null;
        }

        const breadcrumbs = [...(currentLevel.breadcrumbs || [])];
        breadcrumbs.push({
            id: ubicacion.tokenIdentificador,
            name: ubicacion.nombre,
            nodeType: 'ubicacion',
        });

        return breadcrumbs;
    }

    private buildAncestorChain(ubicacion: UbicacionJerarquiaDTO): Observable<UbicacionBreadcrumb[]> {
        if (!ubicacion.ubicacionJerarquiaPadre?.tokenIdentificador) {
            // Base case: no parent, return only current ubicacion
            return of([
                {
                    id: ubicacion.tokenIdentificador,
                    name: ubicacion.nombre,
                    nodeType: 'ubicacion',
                },
            ]);
        }

        // Recursive case: fetch parent and build chain
        return this.getUbicacionById(ubicacion.ubicacionJerarquiaPadre.tokenIdentificador).pipe(
            switchMap((padre) => {
                return this.buildAncestorChain(padre).pipe(
                    map((ancestors) => [
                        ...ancestors,
                        {
                            id: ubicacion.tokenIdentificador,
                            name: ubicacion.nombre,
                            nodeType: 'ubicacion' as const,
                        },
                    ])
                );
            })
        );
    }

    private mapCentroToEntry(centro: JerarquiaDTO): UbicacionNodoItem {
        return {
            id: centro.tokenIdentificador,
            name: centro.nombre,
            description: centro.nemonico,
            tipoNombre: 'Centro',
            nodeType: 'centro',
            hasChild: true,
            isLeaf: false,
            isReadonly: true,
            raw: centro,
        };
    }

    private mapUbicacionToEntry(ubicacion: UbicacionJerarquiaDTO): UbicacionNodoItem {
        const isLeaf = ubicacion.jerarquiaTipo?.nemonico === UBICACION_TIPO_CELDA;

        return {
            id: ubicacion.tokenIdentificador,
            name: ubicacion.nombre,
            description: ubicacion.descripcion,
            tipoNombre: ubicacion.jerarquiaTipo?.nombre || 'Ubicación',
            nodeType: 'ubicacion',
            hasChild: !isLeaf,
            isLeaf,
            isReadonly: false,
            raw: ubicacion,
        };
    }

    showError(error: unknown): void {
        const message = error instanceof Error ? error.message : 'Ocurrio un error inesperado';
        this.dialogMensajeService.mensajeErrorConTitulo('Atencion', message);
    }
}
