import { CampoDTO } from "../campoDTO.model";
import { UsuarioSistemaDTO } from "../seguridad/usuarioSistemaDTO.model";

export class ProcesoDTO extends CampoDTO {
  idProceso: number;
  nombre: string;
  version: number;
  nemonico: string;
  pasos: PasoDTO[];
  fecCreacion: string;
}

export class PasoDTO extends CampoDTO {
  idPaso: number;
  nombre: string;
  url: string;
  porcentajeAvance: number;
  orden: number;
  pasoAnterior: PasoDTO;
  pasoSiguiente: PasoDTO;
  pasoSubsanacion: PasoDTO;
  jsonCondicional: string;
  rolUsuario: string;
  requiereNotificacionCorreo: boolean;
  rolUsuarioNotificacion: string;
  removido: boolean;
  pasoSalto: number;
  pasoUsuarioList: PasoUsuarioDTO[] = [];
}

export class PasoUsuarioDTO extends CampoDTO {
  usuarioSistema: UsuarioSistemaDTO;
}