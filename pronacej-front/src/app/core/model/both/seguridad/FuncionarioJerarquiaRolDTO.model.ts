import { CampoDTO } from "../campoDTO.model";

export class FuncionarioJerarquiaRolDTO extends CampoDTO {
  /** Identificador de la jerarquía */
  declare idJerarquia: number;
  /** Nombre de la jerarquía (solo para mostrar) */
  declare jerarquia?: string;
  /** Token identificador de la jerarquía */
  declare tokenIdentificadorJerarquia: string;

  /** Identificador del rol */
  declare idRol: number;
  /** Nombre del rol (solo para mostrar) */
  declare rol?: string;
  /** Token identificador del rol */
  declare tokenIdentificadorRol: string;
  declare tokenIdentificadorCargo?: string;


  /** Flag para distinguir vista/edición si lo necesitas */
  esVisualizacion? = false;
}
