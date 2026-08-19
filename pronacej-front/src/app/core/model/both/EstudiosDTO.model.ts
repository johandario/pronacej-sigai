import { RegistroInstitucionDTO } from "./RegistroInstitucionDTO.model";

export interface EstudiosDTO {
  idEstudios?: number;
  tokenIdentificador?: string;
  fechaCreacion?: Date;
  fechaInicioEstudios?: Date | string;
  cicloAcademicoActual?: string;
  convenioPronacej?: boolean;
  independiente?: boolean;
  registroInstitucion?: RegistroInstitucionDTO;
  tokenFichaIdentificacion?: string;
  idFichaIdentificacion?: number;
  nombreInstitucion?: string;
  rucInstitucion?: string;
  inicioEstudios?: string;

}