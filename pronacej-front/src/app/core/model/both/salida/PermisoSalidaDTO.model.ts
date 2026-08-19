import { CatalogoDTO } from "../catalogoDTO.model"; 
import { CampoDTO } from "../campoDTO.model";
import { JerarquiaDTO } from "../jerarquiaDTO.model";



export class PermisoSalidaDTO extends CampoDTO{
    idPermisoSalida: number;
    tokenFichaIdentificacion?: string;
    fechaHoraSalida?: string | Date;
    fechaHoraRegreso?: string | Date;
    tipoSalidaLugar?: string;
    usuarioSalida?: string;
    nroDocumento?: string; 
    observaciones?: string;
    tipoSalida?: CatalogoDTO;
    frecuenciaSalida?: CatalogoDTO;
    actividades?: ActividadSalidaDTO[] = [];
    isComplete?: boolean;
    estadoEvento?: CatalogoDTO;
    centro?: JerarquiaDTO;
    otrosSalida?: string; 
    idRegistroSalida?: number;
}

export class ActividadSalidaDTO {
    descripcion: string;
}