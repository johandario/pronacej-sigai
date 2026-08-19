import { CampoDTO } from "./campoDTO.model";

export class LocalidadDTO extends CampoDTO {
    declare nombre: string;
    declare tipoLocalidad: string;
    declare nemonico: string;

    declare rutaUbigeo: string;
    declare hijos?: LocalidadDTO[];

    declare tieneHijos?: boolean;
    declare ubigeo: string;

    declare tokenIdentificadorPadre?: string;

    declare tokenIdentificadorLocalidadPadre?: string;
    declare nemonicoTipoLocalidad?: string;
}