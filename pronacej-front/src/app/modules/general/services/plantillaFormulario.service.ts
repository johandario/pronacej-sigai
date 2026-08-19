import { Injectable } from '@angular/core';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { PlantillaFormularioDTO } from 'app/core/model/both/plantillaFormularioDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class PlantillaFormularioService {
    private path = '/plantilla-formulario';

    constructor(private backendService: BackendService) {}

    obtenerPlantillasFormulario(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<PlantillaFormularioDTO>>
    > {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    crearPlantillaFormulario(
        plantillaFormularioDTO: PlantillaFormularioDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<PlantillaFormularioDTO>> {
        let endPoint = this.path + '/crear';
        return this.backendService.postFinal(
            endPoint,
            plantillaFormularioDTO,
            nemonicoMenu
        );
    }

    eliminarPlantillaFormulario(
        plantillaFormularioDTO: PlantillaFormularioDTO,
        nemonicoMenu: string
    ):Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(
            endPoint,
            plantillaFormularioDTO,
            nemonicoMenu
        );
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
