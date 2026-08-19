import { CampoDTO } from "./campoDTO.model";

export class CargosJerarquiaDTO extends CampoDTO {

    declare tokenIdentificadorJerarquia: string;
    declare esJefe: boolean;
    declare nombre: string;
    declare idJerarquia: number;

}