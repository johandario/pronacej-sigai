import { CampoDTO } from "../campoDTO.model";

export class ContestacionDTO extends CampoDTO {
  idContestacion?: number;
  idPregunta?: number;
  idRespuesta?: number;
  contestacion: string;
  observacion?: string;
  critico?: boolean;
}