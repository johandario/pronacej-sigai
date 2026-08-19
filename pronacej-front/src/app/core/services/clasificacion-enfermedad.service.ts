import { Injectable } from '@angular/core';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';
import { ClasificacionEnfermedadDTO, ClasificacionEnfermedadRequest } from '../model/both/clasificacionEnfermedadDTO.model';

@Injectable({ providedIn: 'root' })
export class ClasificacionEnfermedadService {
    private readonly path = '/clasificacion-enfermedad';

    constructor(private readonly backendService: BackendService) {}

    obtenerClasificacionEnfermerdades(
        request: ClasificacionEnfermedadRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ClasificacionEnfermedadDTO[]>> {
        let endPoint = this.path + '/obtenerClasificacionEnfermerdades';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }    

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}