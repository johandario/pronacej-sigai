import { Injectable } from '@angular/core';
import { EdadEstadisticaDTO } from 'app/core/model/both/EdadEstadisticaDTO.model';
import { EstadoAdolescenteEstadisticoDTO } from 'app/core/model/both/EstadoAdolescenteEstadisticoDTO.model';
import { FichaIdentificacionDTO, FichaIdentificacionResumenDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { ReportesDTO } from 'app/core/model/both/ReportesDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionRequestFichaIdentificacion } from 'app/core/model/request/PaginacionRequestFichaIdentificacion.model';
import { ValidarIngresoFichaRequest } from 'app/core/model/request/ValidarIngresoFichaRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class FichaIdentificacionService {
    private path = '/ficha-identificacion';

    private datosFichaSubject = new BehaviorSubject<any>(null);
    datosFicha$ = this.datosFichaSubject.asObservable();

    private fotoPerfilSubject = new BehaviorSubject<any>(null);
    fotoPerfil$ = this.fotoPerfilSubject.asObservable();

    constructor(private backendService: BackendService) {}

    /**
     * Obten las fichas de identificacion disponibles para el sistema
     *
     * @param nemonicoFichaIdentificacion string nemonico de una ficha de ingreso del sistema
     *
     * @returns Observable<Navigation>
     */
    obtenerFichasIdentificacionPaginado(
        paginacionRequest: PaginacionRequestFichaIdentificacion,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<FichaIdentificacionDTO>>
    > {
        let endPoint = this.path + '/obtenerFichasIdentificacionPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Obtiene las fichas de identificación resumidas disponibles para el sistema
     * 
     * @param paginacionRequest objeto de paginación y filtros para la consulta
     * @param nemonicoMenu      nemónico de un menú del sistema
     * @returns 
     */
    obtenerFichasIdentificacionResumido(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<FichaIdentificacionResumenDTO>>
    > {
        let endPoint = this.path + '/obtenerFichasIdentificacionResumido';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una ficha de identificacion en el sistema con los datos enviados en el request
     *
     * @param fichaIdentificacionDTO FichaIdentificacionDTO datos de la ficha de ingreso a crear
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacion>>
     */
    crearFichaIdentificacion(
        fichaIdentificacionDTO: FichaIdentificacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaIdentificacionDTO>> {
        let endPoint = this.path + '/crearFichaIdentificacion';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacionDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una ficha de identtificacion en el sistema con los datos enviados en el request
     *
     * @param FichaIdentificacionDTO FichaIdentificacionDTO datos de la ficha de identificacion a eliminar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacionDTO>>
     */
    eliminarFichaIdentificacion(
        fichaIdentificacion: FichaIdentificacionDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarFichaIdentificacion';
        return this.backendService.postFinal(
            endPoint,
            fichaIdentificacion,
            nemonicoMenu
        );
    }

    /**
     * Encuentra una ficha de identtificacion en el sistema dado su token identificador
     *
     * @param tokenIdentificador token Identificador uuid para encontrar la ficha de identificacion en el sistema
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacionDTO>>
     */
    obtenerFichaIdentificacionPorTokenIdentificador(
        tokenIdentificador: string,
        nemonicoMenu: string,
    ): Observable<RespuestaPorDefecto<FichaIdentificacionDTO>> {
        let endPoint =
            this.path + '/obtenerFichaIdentificacionPorTokenIdentificador';
            console.log(nemonicoMenu);
            
        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }

    /**
     * Encuentra una ficha de identtificacion en el sistema dado su token identificador
     *
     * @param idFichaIdentificacion id para encontrar la ficha de identificacion en el sistema
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacionDTO>>
     */
    obtenerFichaIdentificacionPorId(
        idFichaIdentificacion: number,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<FichaIdentificacionDTO>> {
        let endPoint = this.path + '/obtenerFichaIdentificacionPorId';
        return this.backendService.postFinal(
            endPoint,
            idFichaIdentificacion,
            nemonicoMenu
        );
    }

    obtenerNombresFichas(
        nemonicoMenu: string,
        tokenCentro: string = null
    ): Observable<RespuestaPorDefecto<FichaIdentificacionDTO[]>> {
        let endPoint = this.path + '/obtenerNombresFichas';

        // Agregar tokenCentro solo si tiene un valor
        if (tokenCentro) {
            endPoint += `?tokenCentro=${tokenCentro}`;
        }

        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * Encuentra una ficha de identtificacion en el sistema dado su token identificador
     *
     * @param tokenIdentificador token Identificador uuid para encontrar la ficha de identificacion en el sistema
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<FichaIdentificacionDTO>>
     */
    obtenerFichaIdentificacionPorNumeroDocumento(
        numeroIdentificador: string,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<FichaIdentificacionDTO>> {
        let endPoint =
            this.path + '/obtenerFichaIdentificacionPorNumeroIdentificacion';
        return this.backendService.postFinal(
            endPoint,
            numeroIdentificador,
            nemonicoMenu
        );
    }

    obtenerEstadisticasEdades(
        nemonicoMenu: string,
        reportesDTO: ReportesDTO
    ): Observable<RespuestaPorDefecto<EdadEstadisticaDTO[]>> {
        let endPoint = this.path + '/obtenerEstadisticasEdades';

        return this.backendService.postFinal(
            endPoint,
            reportesDTO,
            nemonicoMenu
        );
    }

    obtenerEstadisticasEstados(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EstadoAdolescenteEstadisticoDTO[]>> {
        let endPoint = this.path + '/obtenerEstadisticasEstados';
        return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
    }

    obtenerEstadisticasSexo(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EstadoAdolescenteEstadisticoDTO[]>> {
        let endPoint = this.path + '/obtenerEstadisticasSexo';
        return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * Valida si un adolescente puede realizar un ingreso nuevo en el sistema
     *
     * @param request ValidarIngresoFichaRequest datos de la ficha de identificacion a validar
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    validarIngresoNuevo(
        request: ValidarIngresoFichaRequest,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/validarIngresoNuevo';
        return this.backendService.postFinal(
            endPoint,
            request,
            nemonicoMenu
        );
    }

    checkError(error: any) {
        this.backendService.checkError(error);
    }

    actualizacionFicha(datos: any) {
        this.datosFichaSubject.next(datos);
    }

    actualizacionFotoPerfil(datos: any) {
        this.fotoPerfilSubject.next(datos);
    }

 
}
