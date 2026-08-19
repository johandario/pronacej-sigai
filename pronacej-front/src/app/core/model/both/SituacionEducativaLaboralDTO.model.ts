import { AreasSituacionEducativaLaboralOcioDTO } from "./areasSituacionEducativaLaboralOcioDTO.model";
import { CampoDTO } from "./campoDTO.model";
import { LaboralDTO } from "./LaboralDTO.model";
import { SituacionEducativaLaboralOcioDTO } from "./SituacionEducativaLaboralOcioDTO.model";

export class SituacionEducativaLaboralDTO extends CampoDTO {

    declare tokenIdentificadorFichaIdentificacion: string;

    declare listaSituEducLaboOcio: SituacionEducativaLaboralOcioDTO[];
    declare listaLaboral: LaboralDTO[];
    declare areas: AreasSituacionEducativaLaboralOcioDTO;

    declare nombreCompletoUsuarioCreacion: string;

}
