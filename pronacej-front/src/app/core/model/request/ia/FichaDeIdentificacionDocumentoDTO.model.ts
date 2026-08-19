import { CampoDTO } from "../../both/campoDTO.model";
import { CatalogoDTO } from "../../both/catalogoDTO.model";
import { DocumentoDTO } from "../../both/DocumentoDTO.model";
import { FichaIdentificacionDTO } from "../../both/fichaIdentificacionDTO.model";

export class FichaDeIdentificacionDocumentoDTO extends CampoDTO {
    declare documentoDTO: DocumentoDTO;
    declare tipoDeDocumentoFichaDeIdentificacion: CatalogoDTO;
    declare fichaIdentificacionDTO: FichaIdentificacionDTO;
}