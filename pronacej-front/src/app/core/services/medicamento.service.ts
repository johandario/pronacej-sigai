import { Injectable } from '@angular/core';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';
import { MedicamentoDTO, MedicamentoRequest } from '../model/both/EJE/medicamentoDTO.model';

@Injectable({ providedIn: 'root' })
export class MedicamentoService {
    private readonly path = '/medicamento';

    constructor(private readonly backendService: BackendService) {}

    obtenerMedicamentos(
        request: MedicamentoRequest,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<MedicamentoDTO[]>> {
        let endPoint = this.path + '/obtenerMedicamentos';
        return this.backendService.postFinal(endPoint, request, nemonicoMenu);
    }    

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}