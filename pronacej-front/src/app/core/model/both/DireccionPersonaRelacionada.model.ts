import { CampoDTO } from "./campoDTO.model";

export class DireccionPersonaRelacionadaDTO extends CampoDTO{

    declare  direccion : string;
    declare  tipoDireccion : string;
    declare  idDireccion : number;
    declare  idPersonaRelacionada : number;
    declare nombreDireccion: string;

}