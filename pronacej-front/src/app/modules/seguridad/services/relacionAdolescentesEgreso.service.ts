import { Injectable } from '@angular/core';
import { RelacionAdolescentesEgresoDTO } from 'app/core/model/both/relacionEstudiantesEgresoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class RelacionAdolescentesEgresoService {
    private path = '/relacion-adolescentes-egreso';

    constructor(private backendService: BackendService) {}

    /**
     * Obtiene las relaciones de adolescentes para egreso de manera paginada
     * @param paginacionRequest Objeto con parámetros de paginación
     * @param nemonicoMenu Nemonico del menú
     */
    obtenerRelacionesAdolescentesEgresoPaginado(
        paginacionRequest: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<RelacionAdolescentesEgresoDTO>>
    > {
        let endPoint =
            this.path + '/obtenerRelacionesAdolescentesEgresoPaginado';
        return this.backendService.postFinal(
            endPoint,
            paginacionRequest,
            nemonicoMenu
        );
    }

    /**
     * Crea una relación de adolescente para egreso
     * @param relacionAdolescentesEgresoDTO Datos de la relación a crear
     * @param nemonicoMenu Nemonico del menú
     */
    crearRelacionAdolescentesEgreso(
        relacionAdolescentesEgresoDTO: RelacionAdolescentesEgresoDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<RelacionAdolescentesEgresoDTO>> {
        let endPoint = this.path + '/crearRelacionAdolescentesEgreso';
        return this.backendService.postFinal(
            endPoint,
            relacionAdolescentesEgresoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina una relación de adolescente para egreso
     * @param relacionAdolescentesEgresoDTO Datos de la relación a eliminar
     * @param nemonicoMenu Nemonico del menú
     */
    eliminarRelacionAdolescentesEgreso(
        relacionAdolescentesEgresoDTO: RelacionAdolescentesEgresoDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminarRelacionAdolescentesEgreso';
        return this.backendService.postFinal(
            endPoint,
            relacionAdolescentesEgresoDTO,
            nemonicoMenu
        );
    }

    /**
     * Verifica y maneja errores del servicio
     * @param error Error a verificar
     * @param mostrarError Indica si se debe mostrar el error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
