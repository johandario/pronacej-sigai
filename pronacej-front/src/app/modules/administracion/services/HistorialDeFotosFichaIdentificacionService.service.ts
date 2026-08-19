import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { HistorialDeFotosFichaIdentificacionDTO } from 'app/core/model/both/ia/HistorialDeFotosFichaIdentificacionDTO.model';
import { HistorialDeFotosFichaIdentificacionRequest } from 'app/core/model/request/ia/HistorialDeFotosFichaIdentificacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class HistorialDeFotosFichaIdentificacionService {
    private path = '/historial-de-fotos-ficha-identificacion';

    constructor(private backendService: BackendService) {}

    checkError(error: any, mostrarError = true) {
        return this.backendService.checkError(error, mostrarError);
    }

    /**
     * Sube un documento al historial de la ficha de identificacion
     *
     * @param historialDeFotosFichaIdentificacionDTO HistorialDeFotosFichaIdentificacionDTO
     * @param archivo File
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>>
     */
    subirHistorial(
        historialDeFotosFichaIdentificacionDTO: HistorialDeFotosFichaIdentificacionDTO,
        archivo: File,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>> {
        let endPoint = this.path + '/subir-archivos';

        return new Observable(
            (
                subs: Subscriber<
                    RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>
                >
            ) => {
                let formData = new FormData();
                formData.append('documento', archivo);
                this.backendService
                    .crearBodyEncriptado(historialDeFotosFichaIdentificacionDTO)
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
                                            RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>
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

    /**
     * Obten los historiales de la ficha de identificacion
     *
     * @param historialDeFotosFichaIdentificacionRequest HistorialDeFotosFichaIdentificacionRequest
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>>>
     */
    obtener(
        historialDeFotosFichaIdentificacionRequest: HistorialDeFotosFichaIdentificacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<
            PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>
        >
    > {
        let endPoint = this.path + '/obtener';
        return this.backendService.postFinal(
            endPoint,
            historialDeFotosFichaIdentificacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Eliminar los historiales de la ficha de identificacion
     *
     * @param historialDeFotosFichaIdentificacionDTO HistorialDeFotosFichaIdentificacionDTO
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<PaginacionResponse<HistorialDeFotosFichaIdentificacionDTO>>>
     */
    eliminar(
        historialDeFotosFichaIdentificacionDTO: HistorialDeFotosFichaIdentificacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            historialDeFotosFichaIdentificacionDTO,
            nemonicoMenu
        );
    }

    /**
     * Obten los historiales de la ficha de identificacion
     *
     * @param tokenIdentificador tokenIdentificador de la ficha
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>>
     */
    obtenerFotoPerfil(
        tokenIdentificador: String,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<HistorialDeFotosFichaIdentificacionDTO>> {
        let endPoint = this.path + '/obtenerFotoPerfil';

        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }
}
