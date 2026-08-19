import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { ActaExternamientoDTO } from 'app/core/model/both/ia/actaExternamientoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class ActaExternamientoService {
    private path = '/actaExternamiento';

    constructor(private backendService: BackendService) {}

    obtenerActasExternamiento(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ActaExternamientoDTO>>
    > {
        let endPoint = this.path + '/obtenerActasExternamiento';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearActaExternamiento(
        psicologicoDTO: ActaExternamientoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/crearActaExternamiento';
        return this.backendService.postFinal(
            endPoint,
            psicologicoDTO,
            nemonicoMenu
        );
    }

    actualizarActaExternamiento(
        psicologicoDTO: ActaExternamientoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/actualizarActaExternamiento';
        return this.backendService.postFinal(
            endPoint,
            psicologicoDTO,
            nemonicoMenu
        );
    }

    /**
     * Sube un acta firmada
     *
     * @param actaExternamientoDTO ActaExternamientoDTO
     * @param archivo File
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<Boolean>>
     */
    subirActaFirmada(
        actaExternamientoDTO: ActaExternamientoDTO,
        archivo: File,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/subirActaFirmada';
        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                let formData = new FormData();
                formData.append('documento', archivo);
                this.backendService
                    .crearBodyEncriptado(actaExternamientoDTO)
                    .then((bodyEncriptado) => {
                        formData.append('body', JSON.stringify(bodyEncriptado));

                        this.backendService
                            .postFormDataBodyEncriptado2(
                                endPoint,
                                formData,
                                nemonicoMenu
                            )
                            .subscribe({
                                next: async (body: BodyEncriptado) => {
                                    let resp =
                                        await this.backendService.desencriptarBdyEncriptado<
                                            RespuestaPorDefecto<Boolean>
                                        >(body);
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

    eliminarActaExternamiento(
        psicologicoDTO: ActaExternamientoDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarActaExternamiento';

        return this.backendService.postFinal(
            endPoint,
            psicologicoDTO,
            nemonicoMenu
        );
    }
}
