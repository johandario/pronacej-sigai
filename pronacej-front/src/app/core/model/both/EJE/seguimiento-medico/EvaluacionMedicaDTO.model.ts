import { CatalogoDTO } from '../../catalogoDTO.model';
import { CriterioEvaluacionMedicaSeguimientoDTO } from '../../criterioEvaluacionMedicaSeguimientoDTO.model';
import { RecetaDTO } from '../recetaDTO.model';

export class EvaluacionMedicaDTO {
    declare tokenIdentificador?: string;
    declare tokenIdFichaMedica: string;
    declare fecha: Date;
    declare talla: string;
    declare peso: string;
    declare numReferencia: string;
    declare recomendacion?: string;

    declare etapa: CatalogoDTO;
    declare tipoEvaluacion: CatalogoDTO;
    declare motivoConsulta?: CatalogoDTO;

    declare criteriosAsociadosSeguimiento?: CriterioEvaluacionMedicaSeguimientoDTO[];
    declare tokensCriteriosEliminar?: string[];
    declare receta?: RecetaDTO;

    declare lugarAtencion?: string;
    declare doctorAtencion?: string;
}
