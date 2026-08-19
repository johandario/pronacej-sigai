import { CampoDTO } from "./campoDTO.model";

export class CatalogoDTO extends CampoDTO {
    declare idCatalogo?: number;
    declare nombre: string;
    declare descripcion: string;
    declare nemonico: string;
    declare codigoExterno?: string;
    declare tokenIdentificadorPadre?: string;

    tieneHijos? = false;

    declare hijos?: CatalogoDTO[];

}
