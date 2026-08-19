import { CampoDTO } from "./campoDTO.model";

export class PlantillaVariableDTO extends CampoDTO {
    declare clave: string;
    declare nombre: string;
    declare valor: string;
    declare orden: number;
    declare tokenIdentificadorPlantillaFormulario?: string;
}