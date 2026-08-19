import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class PlanAsistenciaPostEgresoDTO extends CampoDTO {
  idPlanAsistenciaPostEgreso: number;
  nombreEstado: string;
  estado: CatalogoDTO;
  fechaInicio: Date;
  fechaFin: Date;
  fecInicio: string;
  fecFin: string;
  fecCreacion: string;
  planDetalle: PlanAsistenciaPostEgresoDetalleDTO[] = [];
  tokenFichaIdenticacion: string;
  idFichaIdentificacion: number;  
}

export class PlanAsistenciaPostEgresoDetalleDTO extends CampoDTO {
  idPlanAsistenciaPostEgresoDetalle: number;
  area: CatalogoDTO;
  factores: string;
  objetivoGeneral: string;
  objetivoEspecifico: string;
  actividades: string;
  institucion: string;
  frecuencia: string;
  indicador: string;
}