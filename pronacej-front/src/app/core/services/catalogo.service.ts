import { Injectable } from '@angular/core';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { PaginacionConParametrosRequest } from 'app/core/model/request/PaginacionConParametrosRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { List } from 'lodash';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
    private readonly path = '/catalogo';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener catalogos
     */
    getCatalogos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CatalogoDTO>>> {
        let endPoint = this.path + '/obtenerCatalogos';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Buscar catalogos por nombre
     */
    buscarCatalogos(
        paginacionRequest: PaginacionConParametrosRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CatalogoDTO>>> {
        let endPoint = this.path + '/buscarCatalogos';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Obtener sub catalogos
     */
    getSubCatalogos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CatalogoDTO>>> {
        let endPoint = this.path + '/obtenerSubCatalogos';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Buscar sub catalogos por nombre
     */
    buscarSubCatalogos(
        paginacionRequest: PaginacionConParametrosRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<PaginacionResponse<CatalogoDTO>>> {
        let endPoint = this.path + '/buscarSubCatalogos';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    /*
     * Obtener catálogos por nemónico padre
     */
    getCatalogosPorNemonicPadre(
        nemonicoPadre: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<List<CatalogoDTO>>> {
        let endPoint = this.path + '/obtenerCatalogosPorNemonicoPadre';
        return this.backendService.postFinal(endPoint, nemonicoPadre, nemonicoMenu);
    }

    /*
     * Obtener catálogos principales - CORREGIDO: Cambiar de POST a GET
     */
    obtenerCatalogosPrincipales(
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO[]>> {
        let endPoint = this.path + '/obtenerCatalogosPrincipales';
        // CAMBIO: getFinal en lugar de postFinal porque el backend usa @GetMapping
        return this.backendService.getFinal(endPoint, nemonicoMenu);
    }

    /*
     * Obtener catálogo por token
     */
    obtenerCatalogo(
        tokenIdentificador: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO>> {
        let endPoint = this.path + '/obtenerCatalogo';
        return this.backendService.getFinal(endPoint + '?tokenIdentificador=' + tokenIdentificador, nemonicoMenu);
    }

    /*
     * Obtener catálogo por nemónico
     */
    obtenerCatalogoPorNemonico(
        nemonico: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO>> {
        let endPoint = this.path + '/obtenerCatalogoPorNemonico';
        return this.backendService.getFinal(endPoint + '?nemonico=' + encodeURIComponent(nemonico), nemonicoMenu);
    }

    /*
     * Obtener hijos de un catálogo por nemónico (compatibilidad con código existente)
     */
    obtenerHijos(
        nemonicoPadre: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO[]>> {
        let endPoint = this.path + '/obtenerCatalogosPorNemonicoPadre';
        return this.backendService.postFinal(endPoint, nemonicoPadre, nemonicoMenu);
    }

    /*
     * Obtener hijos de un catálogo por token
     */
    obtenerHijos2(
        tokenIdentificador: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO[]>> {
        let endPoint = this.path + '/obtenerHijos2';
        return this.backendService.getFinal(endPoint + '?tokenIdentificador=' + tokenIdentificador, nemonicoMenu);
    }

    /*
     * Obtener descendencia de un catálogo
     */
    obtenerDescendencia(
        tokenIdentificador: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO[]>> {
        let endPoint = this.path + '/obtenerDescendencia';
        return this.backendService.getFinal(endPoint + '?tokenIdentificador=' + tokenIdentificador, nemonicoMenu);
    }

    /*
     * Obtener catálogos por filtro de string
     */
    obtenerTodosPorString(
        stringFiltro: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO[]>> {
        let endPoint = this.path + '/obtenerTodosPorString';
        return this.backendService.getFinal(endPoint + '?stringFiltro=' + encodeURIComponent(stringFiltro), nemonicoMenu);
    }

    /**
     * Crear catálogo
     */
    crearCatalogo(
        catalogo: CatalogoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO>> {
        let endPoint = this.path + '/crearCatalogo';
        return this.backendService.postFinal(endPoint, catalogo, nemonicoMenu);
    }

    /**
     * Actualizar catálogo
     */
    actualizarCatalogo(
        catalogo: CatalogoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO>> {
        let endPoint = this.path + '/actualizarCatalogo';
        return this.backendService.postFinal(endPoint, catalogo, nemonicoMenu);
    }

    /**
     * Eliminar catálogo
     */
    eliminarCatalogo(
        catalogo: CatalogoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<CatalogoDTO>> {
        let endPoint = this.path + '/eliminarCatalogo';
        return this.backendService.postFinal(endPoint, catalogo, nemonicoMenu);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}