import { CampoDTO } from "./campoDTO.model";

export class InstrumentoEvaluacionDTO extends CampoDTO {
    declare tokenIdentificadorInformeSeguimiento: string;
    declare tokenIdentificadorTipoInstrumento: string;
}