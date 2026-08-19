import { CampoDTO } from "../../both/campoDTO.model";
import { DocumentoDTO } from "../../both/DocumentoDTO.model";

export class FichaIngresoDocumentoDTO extends CampoDTO{
    declare tokenIdentificadorFichaIngreso: string;
    declare documentoDTO: DocumentoDTO;
    declare tokenFichaIdentificacion: string;
}