import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class DocumentoDTO extends CampoDTO {
    declare mimeType: string;
    declare nombre: string;
    declare tamanioBytes: number;
    declare tipoDocumentoSistema: CatalogoDTO;
    declare descripcion: string;

    declare tipoDeDocumentoSistemaOtro: string;
}