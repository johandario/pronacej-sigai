import { CampoDTO } from "./campoDTO.model";
import { PlantillaVariableDTO } from "./plantillaVariableDTO.model";

export class PlantillaFormularioDTO extends CampoDTO {
    declare formularioString: string;
    declare contenidoHtml: string;
    declare nemonico: string;
    declare descripcion: string;
    declare razon: string;
    declare tokenIdentificadorFormularioRelacionado: string;

    declare listaVariables: PlantillaVariableDTO[];
    declare listaVariablesEliminar: PlantillaVariableDTO[];
}