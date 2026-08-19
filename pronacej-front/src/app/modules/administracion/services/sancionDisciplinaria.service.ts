import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { DocumentoDTO } from 'app/core/model/both/DocumentoDTO.model';
import { SancionDisciplinariaDTO } from 'app/core/model/both/ia/SancionDisciplinariaDTO.model'; 
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})

export class SancionDisciplinariaService {
    private path = '/sancion-disciplinaria';

      constructor(private backendService: BackendService) {}

        crearSancion(
            sancionDTO: SancionDisciplinariaDTO,
            nemonicoMenu: string = ''
            ): Observable<RespuestaPorDefecto<SancionDisciplinariaDTO>> {
            const endPoint = this.path + '/crear';
            return this.backendService.postFinal(endPoint, sancionDTO, nemonicoMenu);
        }

        obtenerSancionPorToken(
            token: string,
            nemonicoMenu: string = ''
        ): Observable<RespuestaPorDefecto<SancionDisciplinariaDTO>> {
            const endPoint = `${this.path}/buscar?ID=${token}`;
            return this.backendService.getFinal(endPoint, nemonicoMenu);
        }

        eliminarSancion(
            sancionDTO: SancionDisciplinariaDTO,
            nemonicoMenu: string = ''
        ): Observable<RespuestaPorDefecto<boolean>> {
            const endPoint = this.path + '/eliminar';
            return this.backendService.postFinal(endPoint, sancionDTO, nemonicoMenu);
        }

        obtenerSancionesPorTokenFicha(
            paginacionRequest: PaginacionRequest,
            nemonicoMenu: string = ''
        ): Observable<RespuestaPorDefecto<PaginacionResponse<SancionDisciplinariaDTO>>> {
            
            console.log(paginacionRequest);
            const endPoint = this.path + '/listado/token';
            console.log(endPoint);
            return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
        }


        /**
             * Sube un informe firmado
             *
             * @param encabezadoDTO InformeDTO
             * @param archivo File
             * @param nemonicoMenu string nemonico menu
             *
             * @return Observable<RespuestaPorDefecto<Boolean>>
             */
            subirDocumentos(
                seguimientoDTO: SancionDisciplinariaDTO,
                files: File[],
                nemonicoMenu: string
            ): Observable<RespuestaPorDefecto<Boolean>> {
                let endPoint = this.path + '/subirDocumentos';
        
                let formData = new FormData();
                if (files != null) {
                    for (let file of files) {
                        formData.append('documentos', file);
                    }
                }
        
                return new Observable(
                    (subs: Subscriber<RespuestaPorDefecto<Boolean>>) => {
                        this.backendService
                            .crearBodyEncriptado(seguimientoDTO)
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

        


        async checkError(error: any, mostrarError = true): Promise<string> {
            return await this.backendService.checkError(error, mostrarError);
        }
}