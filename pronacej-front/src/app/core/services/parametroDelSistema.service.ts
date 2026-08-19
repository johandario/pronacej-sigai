import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ParametroDelSistemaDTO } from '../model/both/parametroDelSistemaDTO.model';
import { RespuestaPorDefecto } from '../model/response/RespuestaPorDefecto.model';
import { BackendService } from './backend.service';

@Injectable({
    providedIn: 'root',
})
export class ParametroDelSistemaService {
    private path = '/parametro-del-sistema';

    constructor(private backendService: BackendService) {}

    /**
     * Obten los parametros del sistema hijo a partir del padre
     *
     * @param parametroDelSistemaDTO ParametroDelSistemaDTO
     * @param nemonicoMenu string nemonico de la accion a realizarse
     *
     * @return Observable<RespuestaPorDefecto<ParametroDelSistemaDTO[]>>
     */
    obtenerParamHijos(
        parametroDelSistemaDTO: ParametroDelSistemaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<ParametroDelSistemaDTO[]>> {
        let endPoint = this.path + '/obtenerParamHijos';

        return this.backendService.postFinal(
            endPoint,
            parametroDelSistemaDTO,
            nemonicoMenu
        );
    }

    /**
     * Obten los parametros del sistema hijo a partir del padre
     *
     * @param nemonico ParametroDelSistemaDTO
     * @param nemonicoMenu string nemonico de la accion a realizarse
     *
     * @return Observable<RespuestaPorDefecto<ParametroDelSistemaDTO[]>>
     */
    obtenerParametroDelSistema(
        nemonico: string,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<ParametroDelSistemaDTO>> {
        let endPoint = this.path + '/obtenerParametroDelSistema';

        return this.backendService.getFinal(
            endPoint,
            { nemonico: nemonico },
            nemonicoMenu
        );
    }

    checkError(error: any, mostrarError = true) {
        this.backendService.checkError(error, mostrarError);
    }
}
