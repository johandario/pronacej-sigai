import { CampoDTO } from "./campoDTO.model";
import { SuspensionVisitasDTO } from "./suspensionVisitasDTO.model";

export class SuspensionVisitasPorPersonaDTO extends CampoDTO {  
  declare tokenIdentificadorFichaPrincipal: string;
  declare listaSuspensionVisitas: SuspensionVisitasDTO[];
}