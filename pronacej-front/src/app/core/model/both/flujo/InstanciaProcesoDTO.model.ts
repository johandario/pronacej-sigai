import { CampoDTO } from "../campoDTO.model";
import { GestionFugaDTO } from "../GestionFugaDTO.model";
import { TrasladoDTO } from "../tras/TrasladoDTO.model";
import { PasoDTO, ProcesoDTO } from "./ProcesoDTO.model";

export class InstanciaProcesoDTO extends CampoDTO {
  idInstanciaProceso: number;
  estado: string;
  descripcion: string;
  proceso: ProcesoDTO;
  tareas: TareaDTO[];
}

export class TareaDTO extends CampoDTO {
  idTarea: number;
  estado: string;
  comentario: string;
  comentarioRechazo: string;
  url: string;
  paso: PasoDTO;
  orden: number;
  rolUsuarioEnvia: string;
  rolUsuarioRecibe: string;
  nombreProceso: string;
  fechaEdicion: Date;
  tipo: string;
  descripcion: string;
  completada?: boolean;
  editable?: boolean;
}

export class TareaTrasladoDTO {
  tarea: TareaDTO;
  traslado: TrasladoDTO;
}


export class TareaEventoFugaDTO {
  tarea: TareaDTO;
  eventoFuga: GestionFugaDTO;
}