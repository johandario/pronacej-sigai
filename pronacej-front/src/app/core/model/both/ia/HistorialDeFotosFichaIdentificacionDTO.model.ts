import { CampoDTO } from "../campoDTO.model";
import { CatalogoDTO } from "../catalogoDTO.model";
import { CarpetaDTO } from "../documento/CarpetaDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";
import { FichaIdentificacionDTO } from "../fichaIdentificacionDTO.model";

export class HistorialDeFotosFichaIdentificacionDTO extends CampoDTO {
    declare tipo: CatalogoDTO;

    declare fichaIdentificacionDTO: FichaIdentificacionDTO;
    declare documentoDTO: DocumentoDTO;
    declare carpetaDTO: CarpetaDTO;

}