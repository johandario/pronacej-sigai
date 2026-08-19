import { CampoDTO } from "./campoDTO.model"
import { CatalogoDTO } from "./catalogoDTO.model";

export class PertenenciaDTO extends CampoDTO {
  idPertenencia: number;
  estado: CatalogoDTO;
  comentarioEgresos: string;
  comentarioIngresos: string;
  comentarioSalidaEgresos: string;
  comentarioSalidaIngresos: string;
  detalleEgresos: PertenenciaDetalleDTO[];
  detalleIngresos: PertenenciaDetalleDTO[];
  detalleSalidaIngresos: PertenenciaDetalleDTO[];
  tokenFichaIdentificacion: String;
  tokenFichaIngreso: string;
  numArticulosRetirados?: string;
  numArticulosEntregados?: string;
  numArticulosRetiradosSalida?: string;
  articulosRetirados?: string;
  articulosEntregados?: string;
  articulosRetiradosSalida?: string;    
  fecCreacionTexto?: string;
  idFichaIdentificacion: number;
}

export class PertenenciaDetalleDTO extends CampoDTO {
  idPertenenciaDetalle: number;
  nombre: string;
  tipo: CatalogoDTO;
  estado: CatalogoDTO;
  cantidad: number;
  observacion: string;
}