import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class JerarquiaDTO {
    declare id: number;
    declare idJerarquiaPadre: number | null;
    declare jerarquiaPadre: JerarquiaDTO;
    declare nombre: string;
    declare nemonico?: string;
    declare ubigeo?: string;
    declare direccion?: string;
    declare empresa: number;
    declare tokenIdentificadorGenero?: string;
    declare tokenIdentificador?: string;
    declare genero?: CatalogoDTO
    declare esOficinaCentral?: boolean;
    declare hijos?: JerarquiaDTO[];
}