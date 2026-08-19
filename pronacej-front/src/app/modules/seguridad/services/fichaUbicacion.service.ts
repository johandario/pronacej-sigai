import { Injectable } from '@angular/core';
import { FichaUbicacionDTO } from 'app/core/model/both/fichaUbicacion.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class FichaUbicacionService {
    private path = '/ficha-ubicacion';

    constructor(private backendService: BackendService) {
        //this.backendService.actualizarClaves();
    }

    obtenerListaPaginada(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/obtenerListaPaginada';

        return this.backendService.postFinal<
            RespuestaPorDefecto<PaginacionResponse<FichaUbicacionDTO>>
        >(endPoint, paginacionRequest, nemonicoMenu);
    }

    obtenerPorTokenIdentificador(
        tokenIdentificador: string,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/obtenerPorTokenIdentificador';
        return this.backendService.getFinal<
            RespuestaPorDefecto<FichaUbicacionDTO>
        >(endPoint, { tokenIdentificador }, nemonicoMenu);
    }

    crearEditar(
        fichaUbicacion: FichaUbicacionDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<FichaUbicacionDTO>> {
        let endPoint = this.path + '/crearEditar';

        return this.backendService.postFinal<
            RespuestaPorDefecto<FichaUbicacionDTO>
        >(endPoint, fichaUbicacion, nemonicoMenu);
    }

    eliminar(
        fichaUbicacion: FichaUbicacionDTO,
        nemonicoMenu: string
    ) {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal<RespuestaPorDefecto<boolean>>(
            endPoint,
            fichaUbicacion,
            nemonicoMenu
        );
    }
   
    checkError(error: any, mostrarError = true) {
        return this.backendService.checkError(error, mostrarError);
    }
}
