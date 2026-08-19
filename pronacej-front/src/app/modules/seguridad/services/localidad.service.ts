import { Injectable } from '@angular/core';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { BackendService } from 'app/core/services/backend.service';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class LocalidadService {
    private path = '/localidad';

    constructor(private backendService: BackendService) {}

    obtenerHijos(
        nemonicoPadre: string,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<LocalidadDTO[]>> {
        let endPoint = this.path + '/obtenerLocalidadesPorPadre';
        let localidadDTO = new LocalidadDTO();
        localidadDTO.nemonico = nemonicoPadre;
        return this.backendService.postFinal(
            endPoint,
            localidadDTO,
            nemonicoMenu
        );
    }

    obtenerPorTipo(
        nemonicoTipo: string,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<LocalidadDTO[]>> {
        let endPoint = this.path + '/obtenerLocalidadesPorPadre';
        let localidadDTO = new LocalidadDTO();
        localidadDTO.tipoLocalidad = nemonicoTipo;
        return this.backendService.postFinal(
            endPoint,
            localidadDTO,
            nemonicoMenu
        );
    }

    /**
     * Encuentra Localidad segun su ubigeo
     *
     * @param ubigeo ubigeo de la localidad
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<DatosFamiliaresDTO>>
     */
    obtenerLocalidadUbigeo(
        ubigeo: String,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<LocalidadDTO>> {
        console.log(nemonicoMenu);
        
        let endPoint = this.path + '/obtenerLocalidadPorUbigeo';

        return this.backendService.postFinal(endPoint, ubigeo, nemonicoMenu);
    }

    obtenerArbol(
        nemonicoPadre: string,
        nemonicoMenu: string,
        tokenIdentificador = ''
    ): Observable<RespuestaPorDefecto<LocalidadDTO[]>> {
        let endPoint = this.path + '/obtenerLocalidadesArbol';
        let localidadDTO = new LocalidadDTO();
        localidadDTO.nemonico = nemonicoPadre;

        return this.backendService.postFinal(
            endPoint,
            localidadDTO,
            nemonicoMenu
        );
    }

    /**
     * Encuentra Localidad segun su ubigeo
     *
     * @param tokenIdentificador ubigeo de la localidad
     * @param nemonicoMenu string nemonico de un menu del sistema
     *
     * @returns Observable<RespuestaPorDefecto<DatosFamiliaresDTO>>
     */
    obtenerLocalidadTokenIdentificador(
        tokenIdentificador: String,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<LocalidadDTO>> {
        let endPoint = this.path + '/obtenerLocalidadPorTokenIdentificador';

        return this.backendService.postFinal(
            endPoint,
            tokenIdentificador,
            nemonicoMenu
        );
    }

    obtenerDescendencia(
        tokenIdentificador: string,
        nemonicoMenu = ''
    ): Observable<RespuestaPorDefecto<LocalidadDTO[]>> {
        let endPoint = this.path + '/obtenerDescendencia';
        return this.backendService.getFinal(
            endPoint,
            { tokenIdentificador },
            nemonicoMenu
        );
    }

    crearLocalidad(
        localidad: LocalidadDTO,
        nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<LocalidadDTO>> {
        let endPoint = this.path + '/crearLocalidad';
        return this.backendService.postFinal(endPoint, localidad, nemonicoMenu);
    }

    /**
     * Obtiene una localidad por su nemónico
     *
     * @param nemonico nemónico de la localidad
     * @param nemonicoMenu string nemónico del menú del sistema
     *
     * @returns Observable<RespuestaPorDefecto<LocalidadDTO>>
     */
    obtenerLocalidadPorNemonico(
        nemonico: string,
        nemonicoMenu = ''
        ): Observable<RespuestaPorDefecto<LocalidadDTO>> {
            const endPoint = this.path + '/obtenerLocalidadPorNemonico';
            return this.backendService.postFinal(endPoint, nemonico, nemonicoMenu);
    }

    editarLocalidad(
    localidad: LocalidadDTO,
    nemonicoMenu: string
    ): Observable<RespuestaPorDefecto<LocalidadDTO>> {
        const endPoint = this.path + '/editarLocalidad';
        return this.backendService.postFinal(endPoint, localidad, nemonicoMenu);
    }

    async checkError(error: any, mostrarError = true): Promise<string> {
        return await this.backendService.checkError(error, mostrarError);
    }
}
