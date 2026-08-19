import { Injectable } from '@angular/core';
import { GestionFugaDTO } from 'app/core/model/both/GestionFugaDTO.model';
import { BodyEncriptado } from 'app/core/model/both/bodyEncriptado.model';
import { TareaEventoFugaDTO } from 'app/core/model/both/flujo/InstanciaProcesoDTO.model';
import { PaginacionRequest } from 'app/core/model/request/PaginacionRequest.model';
import { PaginacionResponse } from 'app/core/model/response/PaginacionResponse.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class GestionFugaService {
    private readonly path = '/fuga';

    constructor(private readonly backendService: BackendService) {}

    /*
     * Obtener lista de fugas
     */

    obtenerFugas(
        paginacionRequest: PaginacionRequest
    ): Observable<RespuestaPorDefecto<PaginacionResponse<GestionFugaDTO>>> {
        let endPoint = this.path + '/lista';

        return this.backendService.postFinal(endPoint, paginacionRequest, '');
    }

    /*
     * Buscar catalogos por nombre
     */
    obtenerFugasPorTokenID(
        tokenIdentificador: String,
        nemonicoMenu:string
    ): Observable<RespuestaPorDefecto<GestionFugaDTO>> {
        let endPoint = this.path + '/buscar';
        return this.backendService.getFinal(
            endPoint,
            { ID: tokenIdentificador },
            nemonicoMenu
        );
    }

    /**
     * Obtener una Gestión de Fuga por ID
     * @param gestionFugaId ID de la Gestión de Fuga
     * @returns Retorna un objeto GestionFugaDTO
     */
    getGestionFugaById(
        gestionFugaId: string
    ): Observable<RespuestaPorDefecto<GestionFugaDTO>> {
        const endPoint = `${this.path}/obtenerGestionFugaPorId`;

        return this.backendService.postFinal(endPoint, gestionFugaId, '');
    }

    /**
     * Crear o editar Proceso
     */
    crearEditarFuga(
        fuga: TareaEventoFugaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<GestionFugaDTO>> {
        let endPoint = this.path + '/crear';

        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Guardar Borrador
     */
    guardarBorrador(
        fuga: TareaEventoFugaDTO,
        nemonico: string
    ): Observable<RespuestaPorDefecto<GestionFugaDTO>> {
        let endPoint = this.path + '/guardarBorrador';

        return this.backendService.postFinal(endPoint, fuga, nemonico);
    }

    /**
     * Actualizar una Gestión de Fuga existente
     * @param gestionFuga Objeto GestionFugaDTO con los datos actualizados
     * @returns Retorna un objeto GestionFugaDTO
     */
    updateGestionFuga(
        gestionFuga: GestionFugaDTO
    ): Observable<RespuestaPorDefecto<GestionFugaDTO>> {
        const endPoint = `${this.path}/actualizarGestionFuga`;
        return this.backendService.postFinal(endPoint, gestionFuga, "");
    }

    /**
     * Eliminar Fuga
     */
    eliminarFuga(traslado: GestionFugaDTO, nemonico: string):Observable<RespuestaPorDefecto<boolean>> {
        let endPoint = this.path + '/eliminar';
        console.log(endPoint);
        return this.backendService.postFinal(endPoint, traslado, nemonico);
    }

    /**
     * Obtener eventos de fuga por ID de Ficha de Identificación
     * @param idFichaIdentificacion ID de la ficha de identificación
     * @returns Observable con la lista de eventos relacionados
     */
    obtenerFugasPorFichaIdentificacion(
        idFichaIdentificacion: number
    ): Observable<RespuestaPorDefecto<GestionFugaDTO[]>> {
        const endPoint = `${this.path}/buscar-por-ficha`;
        console.log(idFichaIdentificacion);
        return this.backendService.postFinal(endPoint, idFichaIdentificacion, "");
    }

    /**
     * Manejar errores
     * @param error Error recibido
     * @param mostrarError Indica si se debe mostrar el error
     * @returns Retorna el mensaje de error
     */
    async checkError(error: any, mostrarError = true): Promise<string> {
        return this.backendService.checkError(error, mostrarError);
    }



    obtenerFugasPorFichaIdentificacionJson(idFichaIdentificacion: number): Observable<RespuestaPorDefecto<GestionFugaDTO[]>> {
        const endPoint = `${this.path}/buscar-por-ficha`;
        const payload = { idFichaIdentificacion: idFichaIdentificacion }; 
        console.log('Payload enviado a backend:', payload);
        return this.backendService.postFinal(endPoint, payload, "");
      }
      
}
