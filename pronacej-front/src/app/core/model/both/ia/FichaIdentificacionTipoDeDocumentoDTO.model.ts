import { CampoDTO } from "../campoDTO.model";
import { CatalogoDTO } from "../catalogoDTO.model";

export class FichaIdentificacionTipoDeDocumentoDTO extends CampoDTO {

    declare seccionFichaDeIdentificacionDTO: CatalogoDTO;
    declare tipoArchivoSistemaDTO: CatalogoDTO;
    declare requerido: boolean;
}