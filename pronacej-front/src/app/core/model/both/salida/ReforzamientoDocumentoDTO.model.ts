import { CampoDTO } from "../campoDTO.model";
import { CarpetaDTO } from "../documento/CarpetaDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";

export class ReforzamientoDocumentoDTO extends CampoDTO {
    documentoDTOList?: DocumentoDTO[];
    carpetaDTO?: CarpetaDTO;
}
