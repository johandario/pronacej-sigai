import { CampoDTO } from "./campoDTO.model";
import { OrientacionConsejeriaFamiliarDTO } from "./orientacionConsejeriaFamiliarDTO.model";

export class OrientacionConsejeriaPorPersonaDTO extends CampoDTO {

    declare tokenIdentificadorPersonaRelacionada: string;

    declare listaOrientacionesConsejerias: OrientacionConsejeriaFamiliarDTO[];

}