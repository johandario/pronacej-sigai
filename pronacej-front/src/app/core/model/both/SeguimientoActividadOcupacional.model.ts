import { CampoDTO } from "./campoDTO.model";

export class SeguimientoActividadOcupacionalDTO extends CampoDTO{
    tokenIdentificadorActividadOcupacional: string;
    actividad: string;
    vigente: boolean;
    observaciones?: string;
    fechaActividad: Date;
}