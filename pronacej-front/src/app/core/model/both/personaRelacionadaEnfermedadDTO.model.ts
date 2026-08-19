import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";
import { ClasificacionEnfermedadDTO } from "./clasificacionEnfermedadDTO.model";

export class PersonaRelacionadaEnfermedadDTO extends CampoDTO {

    declare tokenTipoEnfermedad: string;
    declare detalle: string;
    declare enfermedadActiva: boolean;
    declare tokenIdentificadorPersona: string;

    declare parentescoPersona: string;
    declare nombreEnfermedad: string;
    declare nombrePersona: string;

    declare id_temporal: number;
    declare parentesco: string;

    declare clasificacionEnfermedad: ClasificacionEnfermedadDTO;
    declare tipoParentesco: CatalogoDTO;    
    declare sexoParentesco: CatalogoDTO;    
}