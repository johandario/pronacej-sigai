import { CampoDTO } from "./campoDTO.model";

export class CometimientoInfraccionDTO extends CampoDTO {
    declare tokenIdentificadorSuspensionVisitas: string;
    declare tokenIdentificadorCausalSuspension: string;
    declare seleccionado: boolean;
}