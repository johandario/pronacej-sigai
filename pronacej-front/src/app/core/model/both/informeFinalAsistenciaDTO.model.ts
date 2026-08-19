import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class InformeFinalAsistenciaDTO extends CampoDTO {
  idInformeFinalAsistencia: number;
  nombreEstado: string;
  estado: CatalogoDTO;
  fechaInicio: Date;
  fechaFin: Date;
  fecInicio: string;
  fecFin: string;
  fecCreacion: string;
  detalle: InformeFinalAsistenciaDetalleDTO[] = [];
  tokenFichaIdenticacion: string;
  idFichaIdentificacion: number;
  tokenPlanAsistencia?: string;
}

export class InformeFinalAsistenciaDetalleDTO extends CampoDTO {
  idInformeFinalAsistenciaDetalle: number;
  area: CatalogoDTO;
  objetivoGeneral: string;
  objetivoEspecifico: string;
  actividades: string;
  descripcionActividad: string;
  logro: string;
  dificultad: string;
}