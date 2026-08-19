import { Injectable } from '@angular/core';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';
import { EspecialidadProductoDTO, EspecialidadProductoRequest } from '../model/both/EJE/especialidadProductoDTO.model';

@Injectable({ providedIn: 'root' })
export class EspecialidadProductoService {
    private readonly path = '/especialidad-producto';

    constructor(private readonly backendService: BackendService) {}

    obtenerEspecialidadProductos(
        request: EspecialidadProductoRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<EspecialidadProductoDTO[]>> {
        let endPoint = this.path + '/obtenerEspecialidadProductos';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }    

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}