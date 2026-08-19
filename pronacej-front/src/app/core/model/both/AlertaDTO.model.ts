import { CampoDTO } from "./campoDTO.model";


export class AlertaDTO extends CampoDTO {
  idAlerta?: number;
  descripcion?: string;
  mensaje?: string;
  ruta?: string;
  tabla?: string;
  campo?: string;
  prioridad?: string;
  unidadTiempo?: string;
  tiempo?: number;
  activo?: Date;
  tokenFichaIdentificacion?: string;
  tokenCentro?: string;
  nombreCentro?: string;
  nombresAdolescente?: string;
  apellidoPaternoAdolescente?: string;
  apellidoMaternoAdolescente?: string;
}
