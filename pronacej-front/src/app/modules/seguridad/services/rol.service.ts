import { Injectable } from '@angular/core';
import { RolDTO } from 'app/core/model/both/seguridad/rolDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class RolService {
    private path = '/rol';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene los roles disponibles en el sistema
     *
     * @param nemonicoMenu default true
     *
     * @returns Observable<RespuestaPorDefecto<RolDTO[]>>
     */
    obtenerRoles(
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<RolDTO[]>> {
        let endPoint = this.path + '/obtenerRoles';
        return this.backendService.getFinal(endPoint, {}, nemonicoMenu);
    }

    /**
     * chequea el error eminitdo por el servicio
     *
     * @param error any
     *
     * @returns String
     */
    async checkError(error: any): Promise<string> {
        return await this.backendService.checkError(error);
    }
}
