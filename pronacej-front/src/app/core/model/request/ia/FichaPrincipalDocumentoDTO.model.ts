import { CampoDTO } from "../../both/campoDTO.model";
import { DocumentoDTOFichaPrincipal } from "../../both/documento/documentoDTOFichaPrincipal.model";
export class FichaPrincipalDocumentoDTO extends CampoDTO{

    declare tokenIdentificadorFichaPrincipal: string;
    declare documentoDTO: DocumentoDTOFichaPrincipal;
}