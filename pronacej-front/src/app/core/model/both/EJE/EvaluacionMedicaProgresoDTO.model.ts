import { CampoDTO } from '../campoDTO.model';
import { CatalogoDTO } from '../catalogoDTO.model';
import { CriterioEvaluacionMedicaProgresoDTO } from './criterioEvaluacionMedicaProgresoDTO.model';

export class EvaluacionMedicaProgresoDTO extends CampoDTO {
    declare tokenIdentificador?: string;
    declare tokenIdFichaMedica: string;
    declare fecha: Date;

    declare estadoNutricional: CatalogoDTO;
    declare tipoEvaluacionProgreso?: CatalogoDTO;

    declare tipoDesnutricion?: CatalogoDTO;

    declare grado: string;
    declare peso: string;
    declare talla: string;
    declare imc: string;
    declare impresionDiagnostico: string;
    declare manejoTerapeutico: string;

    declare clinicamenteSano: Boolean;
    declare enfermo: Boolean;

    declare criteriosEvaluacionProgresoAsociados?: CriterioEvaluacionMedicaProgresoDTO[];
    declare tokensCriteriosEliminar?: string[];

    declare tokenIdentificadorFichaIdentificacion: string;
}
