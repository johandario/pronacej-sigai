import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import {
    ActividadIntervencionDTO,
    ActividadIntervencionSeguimientoDTO,
    CatalogoSimpleDTO,
    PlanTratamientoIndDTO,
    PlanTratamientoIndEspecifDTO,
    PlanTratamientoIndIntervDTO,
    PlanTratamientoIndSeguiAbiertoDTO,
    PlanTratamientoIndSeguiDTO,
    PlanTratamientoSeguimientoDTO,
} from 'app/core/model/both/planTratamientoIndDTO.model';
import { PlanTratamientoIndSeguiAbiertoDocumentoDTO } from 'app/core/model/request/ia/PlanTratamientoIndSeguiAbiertoDocumentoDTO.model';
import { PlanTratamientoIndSeguiAbiertoDocumentoRequest } from 'app/core/model/request/ia/PlanTratamientoIndSeguiAbiertoDocumentoRequest.model';
import { PlanTratamientoIndSeguiDocumentoDTO } from 'app/core/model/request/ia/PlanTratamientoIndSeguiDocumentoDTO.model';
import { PlanTratamientoIndSeguiDocumentoRequest } from 'app/core/model/request/ia/PlanTratamientoIndSeguiDocumentoRequest.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class PlanTratamientoService {
    private path = '/planes-tratamiento';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerPlanesTratamiento(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/lista';

        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndDTO>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    obtenerPlanTratamientoPorId(
        idPlanTratamiento: number,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal<
            RespuestaPorDefecto<PlanTratamientoIndDTO>
        >(endPoint, { param: idPlanTratamiento }, nemonicoMenu);
    }

    obtenerPlanTratamientoActivoPorTokenFicha(
        tokenFicha: string,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/buscar-plan-activo';
        return this.backendService.getFinal<
            RespuestaPorDefecto<PlanTratamientoIndDTO>
        >(endPoint, { param: tokenFicha }, nemonicoMenu);
    }

    crearPlanTratamiento(
        planTratamiento: PlanTratamientoIndDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PlanTratamientoIndDTO>> {
        let endPoint = this.path + '/crear';

        //let planEnviar = this.mapToSend(planTratamiento);

        return this.backendService.postFinalCompressed<
            RespuestaPorDefecto<PlanTratamientoIndDTO>
        >(endPoint, planTratamiento, nemonicoMenu);
    }

    eliminarPlanTratamiento(
        planTratamiento: PlanTratamientoIndDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            planTratamiento,
            nemonicoMenu
        );
    }

    obtenerSeguimientos(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/lista-seguimiento';
        return this.backendService.postFinal<
            RespuestaPorDefecto<
                PaginacionResponse<PlanTratamientoSeguimientoDTO>
            >
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearSeguimiento(
        planSeguimiento: PlanTratamientoSeguimientoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/crear-seguimiento';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoSeguimientoDTO>
        >(endPoint, planSeguimiento, nemonicoMenu);
    }

    eliminarSeguimiento(
        planSeguimiento: PlanTratamientoSeguimientoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/eliminar-seguimiento';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            planSeguimiento,
            nemonicoMenu
        );
    }

    obtenerIntervencion(tokenIdentificador: string, nemonicoMenu = '') {
        let endPoint = this.path + '/obtener-intervencion';

        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoIndIntervDTO>
        >(endPoint, tokenIdentificador, nemonicoMenu);
    }

    actualizarIntervencion(
        planTratamiento: PlanTratamientoIndIntervDTO,
        nemonicoMenu = ''
    ) {
        let endPoint = this.path + '/actualizar-intervencion';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoIndIntervDTO>
        >(endPoint, planTratamiento, nemonicoMenu);
    }

    obtenerActividadesTratamiento(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/lista-actividades';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<ActividadIntervencionDTO>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearActualizarActividad(
        actividad: ActividadIntervencionDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/crear-actividad';
        return this.backendService.postFinal<
            RespuestaPorDefecto<ActividadIntervencionDTO>
        >(endPoint, actividad, nemonicoMenu);
    }

    obtenerSeguimientosActividad(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/lista-seguimiento-actividad';
        return this.backendService.postFinal<
            RespuestaPorDefecto<
                PaginacionResponse<ActividadIntervencionSeguimientoDTO>
            >
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearActualizarSeguimientoActividad(
        seguimiento: ActividadIntervencionSeguimientoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/crear-seguimiento-actividad';
        return this.backendService.postFinal<
            RespuestaPorDefecto<ActividadIntervencionSeguimientoDTO>
        >(endPoint, seguimiento, nemonicoMenu);
    }

    obtenerActividadPorId(
        idActividad: number,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ActividadIntervencionDTO>> {
        let endPoint = this.path + '/obtener-actividad-por-id';
        return this.backendService.postFinal<
            RespuestaPorDefecto<ActividadIntervencionDTO>
        >(endPoint, idActividad, nemonicoMenu);
    }

    eliminarActividadIntervencion(
        idActividad: number,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar-actividad';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            idActividad,
            nemonicoMenu
        );
    }

    obtenerSeguimientosPlanesTratamiento(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/lista-seguimiento-pti';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<PlanTratamientoIndSeguiDTO>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearSeguimientoPlanTratamiento(
        seguimiento: PlanTratamientoIndSeguiDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/crear-seguimiento-pti';
        return this.backendService.postFinalCompressed<
            RespuestaPorDefecto<PlanTratamientoIndSeguiDTO>
        >(endPoint, seguimiento, nemonicoMenu);
    }

    eliminarSeguimientoPlanTratamiento(
        seguimiento: PlanTratamientoIndSeguiDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/eliminar-seguimiento-pti';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            seguimiento,
            nemonicoMenu
        );
    }

    obtenerFichasSeguimientoAbierto(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/lista-seguimientos-abierto';
        return this.backendService.postFinal<
            RespuestaPorDefecto<
                PaginacionResponse<PlanTratamientoIndSeguiAbiertoDTO>
            >
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearFichaSeguimientoAbierto(
        seguimiento: PlanTratamientoIndSeguiAbiertoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/crear-ficha-seguimiento-pti-abierto';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoIndSeguiAbiertoDTO>
        >(endPoint, seguimiento, nemonicoMenu);
    }

    eliminarFichaSeguimientoAbierto(
        seguimiento: PlanTratamientoIndSeguiAbiertoDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/eliminar-ficha-seguimiento-pti-abierto';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoIndSeguiAbiertoDTO>
        >(endPoint, seguimiento, nemonicoMenu);
    }

    subirDocumentoFichaSeguimiento(
        file: File,
        planTratamientoIndSeguiAbiertoDocumentoDTO: PlanTratamientoIndSeguiAbiertoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumentoFichaSeguimientoAbierto';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(
                        planTratamientoIndSeguiAbiertoDocumentoDTO
                    )
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));

                        this.backendService
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                nemonicoMenu
                            )
                            .subscribe({
                                next: async (
                                    bodyEncriptado: BodyEncriptado
                                ) => {
                                    let resp =
                                        await this.backendService.desencriptarBdyEncriptado<
                                            RespuestaPorDefecto<DocumentoDTO>
                                        >(bodyEncriptado);
                                    subs.next(resp);
                                    subs.complete();
                                },
                                error: (error: any) => {
                                    subs.error(error);
                                    subs.complete();
                                },
                            });
                    })
                    .catch((error: any) => {
                        subs.error(error);
                        subs.complete();
                    });
            }
        );
    }

    obtenerDocumentosFichaSeguimiento(
        planTratamientoIndSeguiAbiertoDocumentoRequest: PlanTratamientoIndSeguiAbiertoDocumentoRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentosFichaSeguimientoAbierto';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        >(
            endPoint,
            planTratamientoIndSeguiAbiertoDocumentoRequest,
            nemonicoMenu
        );
    }

    eliminarDocumentoFichaSeguimiento(
        planTratamientoIndSeguiAbiertoDocumentoDTO: PlanTratamientoIndSeguiAbiertoDocumentoDTO,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PlanTratamientoIndSeguiAbiertoDocumentoDTO>
    > {
        let endPoint = this.path + '/eliminarDocumento';

        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoIndSeguiAbiertoDocumentoDTO>
        >(endPoint, planTratamientoIndSeguiAbiertoDocumentoDTO, nemonicoMenu);
    }

    subirDocumentoSeguimiento(
        file: File,
        planTratamientoIndSeguiDocumentoDTO: PlanTratamientoIndSeguiDocumentoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<DocumentoDTO>> {
        let endPoint = this.path + '/subirDocumentoSeguimiento';
        let formData = new FormData();
        formData.append('documento', file);

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<DocumentoDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(
                        planTratamientoIndSeguiDocumentoDTO
                    )
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));

                        this.backendService
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                nemonicoMenu
                            )
                            .subscribe({
                                next: async (
                                    bodyEncriptado: BodyEncriptado
                                ) => {
                                    let resp =
                                        await this.backendService.desencriptarBdyEncriptado<
                                            RespuestaPorDefecto<DocumentoDTO>
                                        >(bodyEncriptado);
                                    subs.next(resp);
                                    subs.complete();
                                },
                                error: (error: any) => {
                                    subs.error(error);
                                    subs.complete();
                                },
                            });
                    })
                    .catch((error: any) => {
                        subs.error(error);
                        subs.complete();
                    });
            }
        );
    }

    obtenerDocumentosSeguimiento(
        planTratamientoIndSeguiDocumentoRequest: PlanTratamientoIndSeguiDocumentoRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentosSeguimiento';
        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>
        >(
            endPoint,
            planTratamientoIndSeguiDocumentoRequest,
            nemonicoMenu
        );
    }

    eliminarDocumentoSeguimiento(
        planTratamientoIndSeguiDocumentoDTO: PlanTratamientoIndSeguiDocumentoDTO,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PlanTratamientoIndSeguiDocumentoDTO>
    > {
        let endPoint = this.path + '/eliminarDocumentoSeguimiento';

        return this.backendService.postFinal<
            RespuestaPorDefecto<PlanTratamientoIndSeguiDocumentoDTO>
        >(endPoint, planTratamientoIndSeguiDocumentoDTO, nemonicoMenu);
    }

    checkError(error: any, mostrarError = true) {
        return this.backendService.checkError(error, mostrarError);
    }

    mapToSend(obj: PlanTratamientoIndDTO): any {
        const clone = structuredClone(obj);

        function simplifyCatalogo(item: any): CatalogoSimpleDTO | any {
            if (item && typeof item === 'object' && 'tokenIdentificador' in item) {
                const simple = new CatalogoSimpleDTO();
                simple.idCatalogo = item.idCatalogo;
                simple.tokenIdentificador = item.tokenIdentificador;
                simple.nemonico = item.nemonico;
                return simple;
            }
            return item;
        }   

        // Reemplazar cada propiedad de tipo CatalogoDTO
        clone.estado = simplifyCatalogo(clone.estado);
        clone.medidasAccesorias = clone.medidasAccesorias.map(simplifyCatalogo);
        
        const mapEspec = (arr: PlanTratamientoIndEspecifDTO[]) =>
            arr.map(e => ({
            ...e,
            dimension: simplifyCatalogo(e.dimension)
            }));
        
        clone.especFactores = mapEspec(clone.especFactores);
        clone.ejecMedidas = mapEspec(clone.ejecMedidas);
        clone.unidadReceptora = mapEspec(clone.unidadReceptora);

        const mapInterv = (arr: PlanTratamientoIndIntervDTO[]) =>
            arr.map(i => ({
            ...i,
            dimension: simplifyCatalogo(i.dimension),
            modalidad: simplifyCatalogo(i.modalidad),
            frecuencia: simplifyCatalogo(i.frecuencia)
            }));

        clone.intervObjetivos = mapInterv(clone.intervObjetivos);
        clone.intervNoCriminogenos = mapInterv(clone.intervNoCriminogenos);
        clone.intervDiferenciada = mapInterv(clone.intervDiferenciada);
        clone.intervMedidas = mapInterv(clone.intervMedidas);

        return clone;
    }
}
