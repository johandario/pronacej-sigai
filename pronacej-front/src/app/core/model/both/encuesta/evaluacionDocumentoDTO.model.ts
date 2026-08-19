import { CarpetaDTO } from "../documento/CarpetaDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";

export class EvaluacionDocumentoDTO {
    nemonicoCarpeta: string;
    carpetaDTO: CarpetaDTO;
    documentoDTOList: DocumentoDTO[];
}
