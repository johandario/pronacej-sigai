import { CampoDTO } from "../campoDTO.model";
import { ContestacionDTO } from "./contestacionDTO.model";
import { EncuestaDTO } from "./encuestaDTO.model";
import { EvaluacionDocumentoDTO } from "./evaluacionDocumentoDTO.model";

export class EncabezadoDTO extends CampoDTO {
  idEncabezado?: number;
  nombre?: string;
  descripcion?: string;
  valorTotal?: number;
  fechaCompletacion?: Date;
  completada?: boolean;
  encuesta: string;
  fichaIdentificacion: string;
  contestaciones?: ContestacionDTO[];
  evaluacionDocumentoDTO?: EvaluacionDocumentoDTO;
  tokenIdentificadorValoracionFinal?: string;
  justificacionValoracion?: string;
  fechaValoracion?: Date;
  soloValoracion?: boolean;
}