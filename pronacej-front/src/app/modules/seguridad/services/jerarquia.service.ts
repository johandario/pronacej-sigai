import { Injectable } from '@angular/core';
import { FichaCentroEstadisticaDTO } from 'app/core/model/both/FichaCentroEstadisticaDTO.model';
import { JerarquiaDTO } from 'app/core/model/both/jerarquiaDTO.model';
import { ReportesDTO } from 'app/core/model/both/ReportesDTO.model';
import { JerarquiasPorNemonicosPadreRequest } from 'app/core/model/request/JerarquiasPorNemonicosPadreRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class JerarquiaService {
    private path = '/jerarquia';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene todas las jerarquías disponibles en el sistema
     *
     * @param nemonicoMenu Nemónico del menú del sistema
     * @returns Observable con la lista de jerarquías
     */
    obtenerJerarquias(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO[]>> {
        let endPoint = this.path + '/obtenerJerarquias';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * Obtiene jerarquías filtradas por nemónico padre
     *
     * @param nemonicoPadre Nemónico de la jerarquía padre (ej: 'CJDR', 'SOA', 'UAPISE')
     * @param nemonicoMenu Nemónico del menú del sistema
     * @param tokenIdentificador Token identificador opcional
     * @returns Observable con la lista de jerarquías hijas
     */
    obtenerJerarquiasPorNemonicoPadre(
        nemonicoPadre: string,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<JerarquiaDTO[]>> {
        let endPoint = this.path + '/obtenerJerarquiasPorNemonicoPadre';
        let jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO.nemonico = nemonicoPadre;
        jerarquiaDTO.tokenIdentificador = tokenIdentificador;
        return this.backendService.postFinal(
            endPoint,
            jerarquiaDTO,
            nemonicoMenu
        );
    }

    /**
     * Obtiene jerarquías filtradas por nemónico padre
     *
     * @param request request con lista de nemonicos a obtener
     * @param nemonicoMenu Nemónico del menú del sistema
     * @param tokenIdentificador Token identificador opcional
     * @returns Observable con la lista de jerarquías hijas
     */
    obtenerJerarquiasPorNemonicoPadreLista(
        request: JerarquiasPorNemonicosPadreRequest,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<Record<string, JerarquiaDTO[]>>> {
        let endPoint = this.path + '/obtenerJerarquiasPorNemonicoPadreLista';        
        return this.backendService.postFinal(
            endPoint,
            request,
            nemonicoMenu
        );
    }

    /**
     * Obtiene jerarquías filtradas por nemónico padre con sus hijos
     *
     * @param nemonicoPadre Nemónico de la jerarquía padre (ej: 'CJDR', 'SOA', 'UAPISE')
     * @param nemonicoMenu Nemónico del menú del sistema
     * @param tokenIdentificador Token identificador opcional
     * @returns Observable con la lista de jerarquías hijas
     */
    obtenerJerarquiasPorNemonicoPadreCompleto(
        nemonicoPadre: string,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<JerarquiaDTO[]>> {
        let endPoint = this.path + '/obtenerJerarquiasPorNemonicoPadreCompleto';
        let jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO.nemonico = nemonicoPadre;
        jerarquiaDTO.tokenIdentificador = tokenIdentificador;
        return this.backendService.postFinal(
            endPoint,
            jerarquiaDTO,
            nemonicoMenu
        );
    }

    /**
     * Obtiene la jerarquía específica del usuario logueado
     * En el sistema multi-jerárquico, cada usuario pertenece a una jerarquía específica
     * El backend determina la jerarquía correcta basándose en el JWT del usuario
     *
     * @param nemonicoMenu Nemónico del menú del sistema
     * @returns Observable con la jerarquía del usuario logueado
     */
    obtenerJerarquiaPorNumeroDeDocumento(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO>> {
        let endPoint = this.path + '/obtenerJerarquiaPorNumeroDeDocumento';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * Crea una nueva jerarquía en el sistema
     *
     * @param jerarquiaDTO Datos de la jerarquía a crear
     * @param nemonicoMenu Nemónico del menú del sistema
     * @returns Observable con la respuesta de creación
     */
    crearJerarquia(
        jerarquiaDTO: JerarquiaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO>> {
        let endPoint = this.path + '/crearJerarquia';
        return this.backendService.postFinal(
            endPoint,
            jerarquiaDTO,
            nemonicoMenu
        );
    }

    /**
     * Actualiza una jerarquía existente en el sistema
     *
     * @param jerarquiaDTO Datos de la jerarquía a actualizar
     * @param nemonicoMenu Nemónico del menú del sistema
     * @returns Observable con la respuesta de actualización
     */
    actualizarJerarquia(
        jerarquiaDTO: JerarquiaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO>> {
        let endPoint = this.path + '/actualizarJerarquia';
        return this.backendService.postFinal(
            endPoint,
            jerarquiaDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una jerarquía del sistema
     *
     * @param jerarquiaDTO Datos de la jerarquía a eliminar
     * @param nemonicoMenu Nemónico del menú del sistema
     * @returns Observable con la respuesta de eliminación
     */
    removerJerarquia(
        jerarquiaDTO: JerarquiaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO>> {
        let endPoint = this.path + '/removerJerarquia';
        return this.backendService.postFinal(
            endPoint,
            jerarquiaDTO,
            nemonicoMenu
        );
    }

    /**
     * Obtiene jerarquías filtradas automáticamente por el nemónico del padre
     * asociado a la jerarquía del funcionario logueado
     * En el sistema multi-jerárquico, esto retorna las jerarquías hermanas del usuario
     *
     * @param nemonicoMenu Nemónico del menú del sistema
     * @returns Observable con la lista de jerarquías relacionadas al funcionario
     */
    obtenerJerarquiasPorJerarquiaPadreFuncionario(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<JerarquiaDTO[]>> {
        const endPoint =
            this.path + '/obtenerJerarquiasPorJerarquiaPadreFuncionario';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * Obtiene jerarquías filtradas por token identificador del padre
     *
     * @param nemonicoPadre Nemónico de la jerarquía padre
     * @param nemonicoMenu Nemónico del menú del sistema
     * @param tokenIdentificador Token identificador del padre
     * @returns Observable con la lista de jerarquías hijas
     */
    obtenerJerarquiasPorTokenPadre(
        nemonicoPadre: string,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<JerarquiaDTO[]>> {
        let endPoint = this.path + '/obtenerJerarquiasPorTokenPadre';
        let jerarquiaDTO = new JerarquiaDTO();
        jerarquiaDTO.nemonico = nemonicoPadre;
        jerarquiaDTO.tokenIdentificador = tokenIdentificador;
        return this.backendService.postFinal(
            endPoint,
            jerarquiaDTO,
            nemonicoMenu
        );
    }

    /**
     * Obtiene estadísticas de fichas agrupadas por centro
     * Útil para reportes y dashboards del sistema multi-jerárquico
     *
     * @param nemonicoMenu Nemónico del menú del sistema
     * @param reportesDTO Parámetros del reporte
     * @returns Observable con las estadísticas por centro
     */
    obtenerEstadisticasFichasPorCentro(
        nemonicoMenu: string,
        reportesDTO: ReportesDTO
    ): Observable<RespuestaPorDefecto<FichaCentroEstadisticaDTO[]>> {
        let endPoint = this.path + '/obtenerEstadisticasFichasPorCentro';
        return this.backendService.postFinal(
            endPoint,
            reportesDTO,
            nemonicoMenu
        );
    }

    /**
     * Maneja los errores del servicio
     *
     * @param error Error a manejar
     * @returns Resultado del manejo del error
     */
    async checkError(error: any): Promise<string> {
        return await this.backendService.checkError(error);
    }
}