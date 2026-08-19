import { Injectable } from '@angular/core';
import { InformeFinalAsistenciaDTO } from 'app/core/model/both/informeFinalAsistenciaDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class InformeFinalAsistenciaService {
    private path = '/informe-final-asistencia';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerInformes(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<InformeFinalAsistenciaDTO>>
    > {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearInforme(
        planTratamiento: InformeFinalAsistenciaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<InformeFinalAsistenciaDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            planTratamiento,
            nemonicoMenu
        );
    }

    eliminarInforme(
        planTratamiento: InformeFinalAsistenciaDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            planTratamiento,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
