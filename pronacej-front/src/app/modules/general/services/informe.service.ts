import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { CampoInformeDTO } from 'app/core/model/both/informe/campoInformeDTO.model';
import { InformeDTO } from 'app/core/model/both/informe/informeDTO.model';
import { PlantillaInformeDTO } from 'app/core/model/both/informe/plantillaInformeDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformeService {
    private path = '/informe';

    constructor(private backendService: BackendService) {}

    obtenerInformes(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<InformeDTO>>> {
        let endPoint = this.path + '/obtenerInformes';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerInformesPorToken(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<InformeDTO>>> {
        let endPoint = this.path + '/obtenerInformesPorToken';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearInforme(
        informeDTO: InformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<InformeDTO>> {
        let endPoint = this.path + '/crearInforme';
        return this.backendService.postFinal(
            endPoint,
            informeDTO,
            nemonicoMenu
        );
    }

    crearInformePorToken(
        informeDTO: InformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<InformeDTO>> {
        let endPoint = this.path + '/crearInformePorToken';
        return this.backendService.postFinal(
            endPoint,
            informeDTO,
            nemonicoMenu
        );
    }

    actualizarInforme(
        informeDTO: InformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<InformeDTO>> {
        let endPoint = this.path + '/actualizarInforme';
        return this.backendService.postFinal(
            endPoint,
            informeDTO,
            nemonicoMenu
        );
    }

    /**
     * Sube un informe firmado
     *
     * @param informeDTO InformeDTO
     * @param archivo File
     * @param nemonicoMenu string nemonico menu
     *
     * @return Observable<RespuestaPorDefecto<Boolean>>
     */
    subirInformeFirmado(
        informeDTO: InformeDTO,
        archivo: File,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/subirInformeFirmado';
        return new Observable(
            (subs: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                let formData = new FormData();
                formData.append('documento', archivo);
                this.backendService
                    .crearBodyEncriptado(informeDTO)
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

    obtenerDocumentos(
        request: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<DocumentoDTO>>> {
        let endPoint = this.path + '/obtenerDocumentos';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }

    eliminarInforme(
        informeListaDTO: InformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarInforme';
        return this.backendService.postFinal(
            endPoint,
            informeListaDTO,
            nemonicoMenu
        );
    }

    obtenerListaPlantillas(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<PlantillaInformeDTO>>
    > {
        let endPoint = this.path + '/obtenerListaPlantillas';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    obtenerPlantillas(
        nemonicoMenu: string,
        tokenCentro: string = null
    ): Observable<RespuestaPorDefecto<PlantillaInformeDTO[]>> {
        let endPoint = this.path + '/obtenerPlantillas';

        // Agregar tokenCentro solo si tiene un valor
        if (tokenCentro) {
            endPoint += `?tokenCentro=${tokenCentro}`;
        }

        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    crearPlantilla(
        plantillaInformeDTO: PlantillaInformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PlantillaInformeDTO>> {
        let endPoint = this.path + '/crearPlantilla';
        return this.backendService.postFinal(
            endPoint,
            plantillaInformeDTO,
            nemonicoMenu
        );
    }

    actualizarPlantilla(
        plantillaInformeDTO: PlantillaInformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/actualizarPlantilla';
        return this.backendService.postFinal(
            endPoint,
            plantillaInformeDTO,
            nemonicoMenu
        );
    }

    eliminarPlantilla(
        plantillaInformeDTO: PlantillaInformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<Boolean>> {
        let endPoint = this.path + '/eliminarPlantilla';
        return this.backendService.postFinal(
            endPoint,
            plantillaInformeDTO,
            nemonicoMenu
        );
    }

    obtenerCamposPorPlantilla(
        plantillaInformeDTO: PlantillaInformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<CampoInformeDTO[]>> {
        let endPoint = this.path + '/obtenerCamposPorIdPlantilla';
        return this.backendService.postFinal(
            endPoint,
            plantillaInformeDTO,
            nemonicoMenu
        );
    }

    obtenerCamposPorInforme(
        informeDTO: InformeDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<CampoInformeDTO[]>> {
        let endPoint = this.path + '/obtenerCamposPorIdInforme';
        return this.backendService.postFinal(
            endPoint,
            informeDTO,
            nemonicoMenu
        );
    }

    obtenerCamposPorNemonico(
        nemonicoInforme: string,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<CampoInformeDTO[]>> {
        let endPoint = this.path + '/obtenerCamposPorNemonico';
        return this.backendService.postFinal(
            endPoint,
            nemonicoInforme,
            nemonicoMenu
        );
    }
}
