import { CampoDTO } from "../campoDTO.model";
import { ReforzamientoDocumentoDTO } from "./ReforzamientoDocumentoDTO.model";
import { SesionReforzamientoDTO } from "./SesionReforzamientoDTO.model";

export class ReforzamientoDTO extends CampoDTO {
    idReforzamiento?: number;
    planVida: boolean;
    tokenFichaIdentificacion: string;
    idFichaIdentificacion?: number;
    numeroSesiones?: number;
    fechaUltimaSesion?: Date;
    tipoUltimaSesion?: string;
    responsableUltimaSesion?: string;
    observacionesUltimaSesion?: string;
    sesiones?: SesionReforzamientoDTO[];
    reforzamientoDocumentoDTO?: ReforzamientoDocumentoDTO;
}