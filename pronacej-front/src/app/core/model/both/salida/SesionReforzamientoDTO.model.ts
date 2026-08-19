import { CampoDTO } from "../campoDTO.model";
import { CarpetaDTO } from "../documento/CarpetaDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";

export class SesionReforzamientoDTO extends CampoDTO {
    idSesionReforzamiento?: number;
    tokenReforzamiento?: string;
    fechaSesion: Date;
    nemonicoTipoSesion: string;
    nombretipoSesion?: string;
    nombreResponsable: string;
    observaciones?: string;
    archivo?: string;
    documentoDTO?: DocumentoDTO;
    carpetaDTO?: CarpetaDTO;
}
