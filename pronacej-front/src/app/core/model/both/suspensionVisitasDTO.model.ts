import { CampoDTO } from "./campoDTO.model";
import { CometimientoInfraccionDTO } from "./cometimientoInfraccionDTO.model";

export class SuspensionVisitasDTO extends CampoDTO {
    declare cometimientosInfraccion: CometimientoInfraccionDTO[];
    declare fechaInicio: Date;
    declare fechaFin: Date;
    declare oficioDeSancion: string;
    declare observaciones: string;
    declare tokenIdentificadorFichaPrincipal: string;
}
