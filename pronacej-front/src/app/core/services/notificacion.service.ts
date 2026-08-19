import { Injectable } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';
import { DocumentoDTO } from '../model/both/DocumentoDTO.model';
import { BodyEncriptado } from '../model/both/bodyEncriptado.model';
import { NotificacionDTO } from '../model/both/ia/notificacionDTO.model';
import { PaginacionRequest } from '../model/request/PaginacionRequest.model';
import { PaginacionResponse } from '../model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from '../model/response/RespuestaPorDefecto.model';
import { BackendService } from './backend.service';

@Injectable({
    providedIn: 'root',
})
export class NotificacionService {
    private path = '/notificacion';

    constructor(private backendService: BackendService) {}

    obtenerNotificacionesPorToken(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<NotificacionDTO>>> {
        let endPoint = this.path + '/obtenerNotificacionesPorToken';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Envia un correo electronico y registra el evento en la base de datos
     *
     * @param files File[] archivos a ser adjuntados en el correo electronico
     * @param notificacionEmailDTO NotificacionEmailDTO
     * @param nemonicoMenu string nemonico de la accion a realizarse
     *
     * @return Observable<RespuestaPorDefecto<NotificacionEmailDTO>>
     */
    enviarNotificacion(
        files: File[],
        notificacionEmailDTO: NotificacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<NotificacionDTO>> {
        let endPoint = this.path + '/enviarNotificacion';
        let formData = new FormData();
        if (files != null) {
            for (let file of files) {
                formData.append('documentos', file);
            }
        }

        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<NotificacionDTO>>) => {
                this.backendService
                    .crearBodyEncriptado(notificacionEmailDTO)
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
                                            RespuestaPorDefecto<NotificacionDTO>
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

    obtenerDocumentos(
        request: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }

    checkError(error: any, mostrarError = true) {
        this.backendService.checkError(error, mostrarError);
    }
}
