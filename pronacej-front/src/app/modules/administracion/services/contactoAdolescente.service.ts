import { Injectable } from '@angular/core';
import { ContactoAdolescenteDTO } from 'app/core/model/both/ia/contactoAdolescenteDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class ContactoAdolescenteService {
    private ruta = '/contacto-adolescente';

    constructor(private servicioBackend: BackendService) {}

    /**
     * Obtiene los contactos con adolescentes disponibles para el sistema de manera paginada
     *
     * @param solicitudPaginacion PaginacionRequest objeto con los parámetros de paginación
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<PaginacionResponse<ContactoAdolescenteDTO>>>
     */
    obtenerContactosPaginado(
        solicitudPaginacion: PaginacionRequest,
        nemonicoMenu = ''
    ): Observable<
        RespuestaPorDefecto<PaginacionResponse<ContactoAdolescenteDTO>>
    > {
        let puntoFinal = this.ruta + '/obtenerContactosPaginado';
        return this.servicioBackend.postFinal(
            puntoFinal,
            solicitudPaginacion,
            nemonicoMenu
        );
    }

    /**
     * Crea un contacto con adolescente en el sistema con los datos enviados en el request
     *
     * @param contactoDTO ContactoAdolescenteDTO datos del contacto a crear
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<ContactoAdolescenteDTO>>
     */
    crearContacto(
        contactoDTO: ContactoAdolescenteDTO,
        nemonicoMenu: string = ''
    ): Observable<RespuestaPorDefecto<ContactoAdolescenteDTO>> {
        let puntoFinal = this.ruta + '/crearContacto';
        return this.servicioBackend.postFinal(
            puntoFinal,
            contactoDTO,
            nemonicoMenu
        );
    }

    /**
     * Elimina un contacto con adolescente en el sistema
     *
     * @param contactoDTO ContactoAdolescenteDTO datos del contacto a eliminar
     * @param nemonicoMenu string nemonico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<boolean>>
     */
    eliminarContacto(
        contactoDTO: ContactoAdolescenteDTO,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<boolean>> {
        let puntoFinal = this.ruta + '/eliminarContacto';
        return this.servicioBackend.postFinal(
            puntoFinal,
            contactoDTO,
            nemonicoMenu
        );
    }

    /**
     * Verifica y maneja los errores generales del servicio
     *
     * @param error any error a verificar
     * @param mostrarError boolean indica si se debe mostrar el error
     * @returns any
     */
    verificarError(error: any, mostrarError = true) {
        return this.servicioBackend.checkError(error, mostrarError);
    }
}
