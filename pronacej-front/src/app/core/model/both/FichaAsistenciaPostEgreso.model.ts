import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";
import { PlanAsistenciaPostEgresoDetalleDTO } from "./planAsistenciaPostEgresoDTO";

export class FichaAsistenciaPostEgresoDTO extends CampoDTO {
    tokenIdentificadorFichaIdentificacion?: string;
    tipoFormato?: CatalogoDTO;
    nombreFormato?: string;
    tokenPlanAsistencia?: string;
    detalleFichaAsistenciaPostEgresos: DetalleFichaAsistenciaPostEgresoDTO[] = [];
    planAsistenciaPostEgresoDetalle: PlanAsistenciaPostEgresoDetalleDTO;
    idFichaIdentificacion?: number;
    idPlanAsistenciaPostEgreso?: number;
}

export class DetalleFichaAsistenciaPostEgresoDTO extends CampoDTO{
    idDetalleFichaAsistenciaPostEgreso: number;
    tokenIdentificadorFichaAsistenciaPostEgreso?: string;
    fechaDetalle?: Date;
    descripcionActividad?: string;
    observaciones?: string;
    modalidadDeEntrevista?: CatalogoDTO;
    personaEntrevistada?: CatalogoDTO;
    motivo?: CatalogoDTO;
}