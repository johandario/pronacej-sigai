import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class SituacionEducativaLaboralOcioDTO extends CampoDTO {
    declare tokenIdentificadorFichaIdentificacion: string;
    declare centroEstudios: string;
    declare tokenIdentificadorSituacionEducativa: string;
    declare tokenIdentificadorRendimientoEducativo: string;
    declare tokenIdentificadorModalidadEducativa: string;
    declare tokenIdentificadorModalidadEstudio: string;
    declare tokenIdentificadorNivelEBR: string;
    declare tokenIdentificadorNivelSuperior: string;
    declare tokenIdentificadorNivelEBA: string;
}
