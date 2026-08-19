import { Injectable } from '@angular/core';
import { RegistroInstitucionDTO } from 'app/core/model/both/RegistroInstitucionDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class InstitucionService {
    private readonly path = '/institucion';
    private registroInstitucionSubject =
        new BehaviorSubject<RegistroInstitucionDTO | null>(null);

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerRegistroInstituciones(
        paginacionRequest: PaginacionRequest,
        nemonico: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<RegistroInstitucionDTO>>
    > {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonico);
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerInstitucionesPorTokenID(
        tokenIdentificador: String
    ): Observable<RespuestaPorDefecto<RegistroInstitucionDTO>> {
        let endPoint = this.path + '/buscar';

        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            ''
        );
    }

    /**
     * Crear o editar Proceso
     */
    crearEditarInstitucion(
        fuga: RegistroInstitucionDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<RegistroInstitucionDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Eliminar Fuga
     */
    eliminarInstitucion(traslado: RegistroInstitucionDTO, nemonico: string) {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Transferir datos de registroInstitucion entre secciones
     */
    setRegistroInstitucionData(data: RegistroInstitucionDTO): void {
        this.registroInstitucionSubject.next(data);
    }

    getRegistroInstitucionData(): RegistroInstitucionDTO | null {
        return this.registroInstitucionSubject.value;
    }

    /**
         * Buscar institución por RUC
         */
        obtenerInstitucionPorRuc(
            ruc: string,
            nemonico: string
        ): Observable<RespuestaPorDefecto<RegistroInstitucionDTO>> {
            let endPoint = this.path + '/buscarPorRuc';

            return this.backendService.getFinal(
                endPoint,
                { ruc: ruc },
                nemonico
            );
        }


    
    /**
     * Manejar errores
     * @param error Error recibido
     * @param mostrarError Indica si se debe mostrar el error
     * @returns Retorna el mensaje de error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
