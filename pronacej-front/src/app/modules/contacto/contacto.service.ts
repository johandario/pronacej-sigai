import { Injectable } from '@angular/core';
import { ContactoAdolescenteDTO } from 'app/core/model/both/ContactoAdolescenteDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ContactoAdolescenteService {
    private readonly path = '/contacto-adolescente';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerContactos(
        paginacionRequest: PaginacionRequest
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ContactoAdolescenteDTO>>
    > {
        let endPoint = this.path + '/lista';
        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    obtenerContactoPorTokenID(
        tokenIdentificador: String
    ): Observable<RespuestaPorDefecto<ContactoAdolescenteDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            ''
        );
    }

    crearEditarContacto(
        fuga: ContactoAdolescenteDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<ContactoAdolescenteDTO>> {
        let endPoint = this.path + '/crear';

        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    eliminarContactoAdolescente(
        traslado: ContactoAdolescenteDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        return this.backendService.postFinal(endPoint, traslado, nemonico);
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
