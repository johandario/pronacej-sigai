import { CampoDTO } from "../campoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";

export class SeguimientoConductualDTO extends CampoDTO {
    idSeguimientoConductual?: number;
    tokenEvaluacion: string;
    estable: boolean;
    periodoDesde: Date;
    periodoHasta: Date;
    periodo?: string;
    nemonicoTipoConducta?: string;
    tipoConducta: string;
    descripcionConducta: string;
    accionesAdoptadas: string;
    programa: JerarquiaDTO;
    ambiente: JerarquiaDTO;
}
