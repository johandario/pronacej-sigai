import { Injectable } from '@angular/core';
import { EstudiosDTO } from 'app/core/model/both/EstudiosDTO.model';
import { EstudiosEstadisticoDTO } from 'app/core/model/EstudiosEstadisticoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class EstudiosService {
    private path = '/estudios';

    constructor(private backendService: BackendService) {}

    obtenerListaEstudios(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PaginacionResponse<EstudiosDTO>>> {
        const endPoint = this.path + '/lista';
        return this.backendService.postFinal(endPoint, paginacionRequest, nemonicoMenu);
    }

    crearEstudios(
        estudiosDTO: EstudiosDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EstudiosDTO>> {
        const endPoint = this.path + '/crear';
        return this.backendService.postFinal(endPoint, estudiosDTO, nemonicoMenu);
    }

    eliminarEstudios(
        estudiosDTO: EstudiosDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        const endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, estudiosDTO, nemonicoMenu);
    }

    obtenerEstudios(
        estudiosDTO: EstudiosDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EstudiosDTO>> {
        const endPoint = this.path + '/obtener';
        return this.backendService.postFinal(endPoint, estudiosDTO, nemonicoMenu);
    }

    consultarInstitucionPorRuc(
        estudiosDTO: EstudiosDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<EstudiosDTO>> {
        const endPoint = this.path + '/consultar-institucion-ruc';
        return this.backendService.postFinal(endPoint, estudiosDTO, nemonicoMenu);
    }

    obtenerCantidadUsuariosEstudiando(
    nemonicoMenu: string
): Observable<RespuestaPorDefecto<number>> {
    const endPoint = this.path + '/cantidadUsuariosEstudiando';
    return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
}

        obtenerEstadisticasEstudios(
            nemonicoMenu: string
        ): Observable<RespuestaPorDefecto<EstudiosEstadisticoDTO[]>> {
            const endPoint = this.path + '/estadisticasEstudios';
            return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
        }

    obtenerPorcentajeConvenioPronacej(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<number>> {
        const endPoint = this.path + '/porcentajeConvenioPronacej';
        return this.backendService.postFinal(endPoint, {}, nemonicoMenu);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}