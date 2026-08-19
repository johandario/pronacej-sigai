import { CampoDTO } from "../campoDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class SeguimientoEducativoDTO extends CampoDTO {
    tokenIdentificadorSeguimiento: string;
    documentoDTOList: DocumentoDTO[];
}
