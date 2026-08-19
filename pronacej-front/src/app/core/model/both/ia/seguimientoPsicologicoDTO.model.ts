import { CampoDTO } from "../campoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class SeguimientoPsicologicoDTO extends CampoDTO {
    idSeguimientoPsicologico?: number;
    tokenEvaluacion: string;
    intervencionConcejeria: string;
    accionesRealizar: string;
    comentariosObservaciones?: string;
    programa: JerarquiaDTO;
    ambiente: JerarquiaDTO;
}
