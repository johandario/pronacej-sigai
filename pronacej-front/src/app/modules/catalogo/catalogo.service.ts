import { Injectable } from '@angular/core';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { List } from 'lodash';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
    private readonly path = '/catalogo';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener catálogos por nemónico padre - MÉTODO FALTANTE QUE CAUSABA ERRORES
     */
    getCatalogosPorNemonicPadre(
        nemonicoPadre: string,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<List<CatalogoDTO>>> {
        let endPoint = this.path + '/obtenerCatalogosPorNemonicoPadre';
        return this.backendService.postFinal(endPoint, nemonicoPadre, nemonicoMenu);
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