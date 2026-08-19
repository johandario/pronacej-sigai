import { CampoDTO } from "./campoDTO.model";

export class CriterioEvaluacionMedicaSeguimientoDTO extends CampoDTO {

    declare tokenIdentifidorCriterioPadre: string;
    declare tokenIdentificadorCriterioHijo: string;
    declare detalle: string;
    declare nombreCriterioPadre: string;
    declare nombreCriterioHijo: string;

    declare id_temporal: number;
}