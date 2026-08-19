import { CampoDTO } from '../campoDTO.model';
import { CatalogoDTO } from '../catalogoDTO.model';

export class CriterioEvaluacionMedicaProgresoDTO extends CampoDTO {
    declare tokenIdentifidorCriterioPadre: string;
    declare tokenIdentificadorCriterioHijo: string;

    declare nombreCriterioPadre: string;
    declare nombreCriterioHijo: string;

    declare tokenIdentificadorLado: string;
    declare nombreLado: string;

    declare tokenIdentificadorUbicacion: string;
    declare nombreUbicacion: string;

    declare criterioPadre: CatalogoDTO;
    declare criterioHijo?: CatalogoDTO;
    declare ladoSigno?: CatalogoDTO;
    declare ubiacionSigno?: CatalogoDTO;
    declare presente? : Boolean;

    declare detalle? : string
}

export interface CriterioEvaluacionMedicaProgresoItemImpresionDTO {
  signo_alteracion: string;
  clave: string;
  ubicacion?: string;
  lado?: string;
  presente: 'Sí' | 'No';
}
