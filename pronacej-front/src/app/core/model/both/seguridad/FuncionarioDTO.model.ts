import { CampoDTO } from "../campoDTO.model";
import { FuncionarioJerarquiaRolDTO } from "./FuncionarioJerarquiaRolDTO.model";

export class FuncionarioDTO extends CampoDTO {

    declare nombres: string;
    declare apellidos: string;
    declare email: string;
    declare telefono: string;
    declare tokenIdentificadorTipoDeDocumento: string;
    declare numeroDeDocumento: string;
    declare numeroDeCelular: string;
    declare logo: string;
    declare fechaCreacion: Date;    
    declare idCargo: number;
    declare cargo?: string;
    declare cargoSuperRol?: boolean;
    declare tokenIdentificadorCargo: string;
    declare idDepartamento: number;
    declare departamento?: string;
    declare tokenIdentificadorDepartamento: string;
    bloqueadoRelacion = false;

    esVisualizacion? = false;

    asignaciones: FuncionarioJerarquiaRolDTO[] = [];

    declare numeroCentros?: number;
}