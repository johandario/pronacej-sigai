import { RegistroInstitucionDTO } from "./RegistroInstitucionDTO.model";

export interface TrabajoLaboralDTO {
  idTrabajoLaboral?: number;
  tokenIdentificador?: string;
  fechaCreacion?: Date;
  fechaIngresoLaboral?: Date;
  cargoLaboral?: string;
  tokenFichaIdentificacion?: string;
  idFichaIdentificacion?: number;
  registroInstitucion?: RegistroInstitucionDTO;
  inicioTrabajo?: string;
}