import { CatalogoDTO } from "../catalogoDTO.model";
import { DocumentoDTO } from "../DocumentoDTO.model";

export class DocumentoDTOFichaPrincipal extends DocumentoDTO{

    declare tipoDeDocumentoFicha: CatalogoDTO;
}