import { CampoDTO } from "./campoDTO.model";
import { DocumentoDTO } from "./DocumentoDTO.model";

export class InformeFinalAbiertoDTO extends CampoDTO {
  idInformeFinalAbierto: number;
  fortalecimientoDerechos: string;
  area: string;
  fortalecimientoFamiliar: string;
  intervencion: string;
  enfoque: string;
  cultural: string;
  responsabilidad: string;
  conciencia: string;
  valoracionRiesgo: string;
  conclusionesRecomendaciones: string;
  fechaFinalizacion?: Date;
  completado?: boolean;
  tokenFichaIdenticacion: string;
  idFichaIdentificacion: number;
  medidasList: InformeFinalAbiertoMedidasDTO[] = [];
  documentoDTOList: DocumentoDTO[] = [];
}

export class InformeFinalAbiertoMedidasDTO extends CampoDTO {
  idInformeFinalAbiertoMedidas: number;
  medidaAccesoria: string;
  accion: string;
  objetivo: string;
  analisisCualitativo: string;
}