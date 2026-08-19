import { CampoDTO } from "./campoDTO.model";
import { InstrumentoEvaluacionDTO } from "./instrumentoEvaluacionDTO.model";

export class InformeSeguimientoPIIDTO extends CampoDTO {
    declare motivoIngreso: string;
    declare antecedentesOrganicidad: string;
    declare tecnicasUtilizadas: string;
    declare instrumentosAplicados: string;
    declare observacionConductual: string;
    declare evaluacionPlanPsicologica: string;
    declare evaluacionPlanSocial: string;
    declare evaluacionPlanConductual: string;
    declare evaluacionPlanFamiliar: string;
    declare evaluacionPlanEducativa: string;
    declare evaluacionPlanLaboral: string;
    declare tokenIdentificadorNivelRiesgo: string;
    declare conclusiones: string;
    declare recomendaciones: string;
    declare tokenIdentificadorEstado: string;
    declare tokenIdentificadorInformeTecnico: string;

    // Lista de instrumentos aplicados
    declare listaInstrumentosAplicados: InstrumentoEvaluacionDTO[];

    // Campo auxiliar para control de visualización
    declare esVisualizacion?: boolean;
}