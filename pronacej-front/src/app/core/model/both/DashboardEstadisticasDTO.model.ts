import { EstadisticaItemDTO } from './EstadisticaItemDTO.model';

export class DashboardEstadisticasDTO {
    declare porDelito: EstadisticaItemDTO[];
    declare porEdad: EstadisticaItemDTO[];
    declare porSexo: EstadisticaItemDTO[];
    declare porNacionalidad: EstadisticaItemDTO[];
    declare porDepartamento: EstadisticaItemDTO[];
    declare porDiasInternacion: EstadisticaItemDTO[];
    declare porTipoEnfermedad: EstadisticaItemDTO[];
    declare porGradoInstruccion: EstadisticaItemDTO[];
    declare porNumeroHijos: EstadisticaItemDTO[];
    declare porNumeroCentros: EstadisticaItemDTO[];
}