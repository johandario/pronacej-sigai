import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FichaIdentificacionTipoDeDocumentoDTO } from '../model/both/ia/FichaIdentificacionTipoDeDocumentoDTO.model';
import { RespuestaPorDefecto } from '../model/response/RespuestaPorDefecto.model';
import { TipoDeArchivoSeccionFichaPrincipal } from '../model/response/TipoDeArchivoSeccionFichaPrincipal.model';
import { BackendService } from './backend.service';

@Injectable({
    providedIn: 'root',
})
export class TipoDeIdentificacionTipoDeDocumentoService {
    private path = '/ficha-identificacion-tipo-de-documento';
    constructor(private backendService: BackendService) {}

    /**
     * Obten un documento fisico directament del sistema
     *
     * @para nemonicoSeccionDeFichaDeIdentificacion string nemonico de la seccion de la ficha de identificacion
     * @param nemonicoMenu string nemonico del menu
     *
     * @return Observable<RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>>
     */
    obtenerTiposDeDocumentos(
        nemonicoSeccionDeFichaDeIdentificacion: string,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>
    > {
        let endPoint = this.path + '/obtenerTiposDeDocumentos';
        return this.backendService.getFinal(
            endPoint,
            {
                nemonicoSeccionFichaIdentificacion:
                    nemonicoSeccionDeFichaDeIdentificacion,
            },
            nemonicoMenu
        );
    }

    /**
     * Devuelve una lista de se secciones de ficha principal con la cantidad total de tipos de archivos
     *
     * @param nemonicoMenu string nemonico del menu
     *
     * @return Observable<RespuestaPorDefecto<TipoDeArchivoSeccionFichaPrincipal[]>>
     */
    obtenerResumen(
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<TipoDeArchivoSeccionFichaPrincipal[]>> {
        let endPoint = this.path + '/obtenerResumen';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * Devuelve una lista de FichaIdentificacionTipoDeDocumentoDTO pr token de seccion de ficha principal
     *
     * @param tokenSeccionFichaPrincipal string token de la seccion de la ficha principal
     *
     * @return Observable<FichaIdentificacionTipoDeDocumentoDTO[]>>
     */
    obtenerPorSeccionFichaPrincipal(
        tokenSeccionFichaPrincipal: string,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO[]>
    > {
        let endPoint = this.path + '/obtenerPorSeccionFichaPrincipal';
        return this.backendService.getFinal(
            endPoint,
            {
                tokenSeccionFichaPrincipal: tokenSeccionFichaPrincipal
                    ? tokenSeccionFichaPrincipal
                    : '',
            },
            nemonicoMenu
        );
    }

    /**
     * Crea una nueva seccion de la ficha principal con un nuevo tipo de documento del sistema
     *
     * @param fichaIdentificacionTipoDeDocumentoDTO FichaIdentificacionTipoDeDocumentoDTO
     *
     * @return Observable<FichaIdentificacionTipoDeDocumentoDTO>
     */
    crear(
        fichaIdentificacionTipoDeDocumentoDTO: FichaIdentificacionTipoDeDocumentoDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO>> {
        let endPoint = this.path + '/crear';

        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionTipoDeDocumentoDTO,
            nemonicoMenu
        );
    }

    /**
     *  Edita una seccion de la ficha principal con un nuevo tipo de documento del sistema
     *
     * @param fichaIdentificacionTipoDeDocumentoDTO FichaIdentificacionTipoDeDocumentoDTO
     *
     * @return Observable<FichaIdentificacionTipoDeDocumentoDTO>
     */
    editar(
        fichaIdentificacionTipoDeDocumentoDTO: FichaIdentificacionTipoDeDocumentoDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO>> {
        let endPoint = this.path + '/editar';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionTipoDeDocumentoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una seccion de la ficha principal con un nuevo tipo de documento del sistema
     *
     * @param fichaIdentificacionTipoDeDocumentoDTO FichaIdentificacionTipoDeDocumentoDTO
     *
     * @return Observable<FichaIdentificacionTipoDeDocumentoDTO>
     */
    eliminar(
        fichaIdentificacionTipoDeDocumentoDTO: FichaIdentificacionTipoDeDocumentoDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<FichaIdentificacionTipoDeDocumentoDTO>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionTipoDeDocumentoDTO,
            nemonicoMenu
        );
    }

    checkError(error: any, mostrarError = true) {
        return this.backendService.checkError(error, mostrarError);
    }
}
