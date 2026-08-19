import { CampoDTO } from "./campoDTO.model";
import { ClasificacionEnfermedadDTO } from "./clasificacionEnfermedadDTO.model";

export class FichaMedicaEnfermedadDTO extends CampoDTO{
    
    declare tokenTipoEnfermedad: string;
    declare detalle: string;
    declare enfermedadActiva: boolean;

    declare nombreEnfermedad: string;
    declare id_temporal: number;

    declare edadPresente: string;
    declare tratamiento: string;

    declare fechaAparicion: Date;
    
    declare clasificacionEnfermedad: ClasificacionEnfermedadDTO;
}