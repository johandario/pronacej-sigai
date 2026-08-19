import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class LaboralDTO extends CampoDTO {

    declare tokenIdentificadorFichaIdentificacion: string;
    declare experienciaLaboral: string;
    declare tokenIdentificadorOcupacionLaboral: string;
    declare tokenIdentificadorModalidadLaboral: string;
}
