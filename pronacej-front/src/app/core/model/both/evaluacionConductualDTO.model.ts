import { CampoDTO } from "./campoDTO.model";
import { CondHistViolDTO } from "./condHistViolDTO.model";
import { SituPersCaraPersDTO } from "./situPersCaraPersDTO.model";

export class EvaluacionConductualDTO extends CampoDTO {

    declare tokenIdentificadorFichaIdentificacion: string;
    declare fechaCreacion: Date;

    declare listaSituPersCaraPers: SituPersCaraPersDTO[];
    declare listaCondHistViolDTO: CondHistViolDTO[];

    declare esVisualizacion?: boolean;
}