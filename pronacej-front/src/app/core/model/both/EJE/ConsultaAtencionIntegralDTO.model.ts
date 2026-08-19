import { CampoDTO } from "../campoDTO.model";
import { OrdenMedicaDTO } from "./ordenMedicaDTO.model";
import { RecetaDTO } from "./recetaDTO.model";

export class ConsultaAtencionIntegralDTO extends CampoDTO {
    declare tokenIdFichaMedica?: string;
    declare fechaInicio?: Date;
    declare observaciones?: string;
    declare motivoConsulta?: string;
    declare edad?: string;
    declare tipoEnfermedad?: string;
    declare formaDeInicio?: string;
    declare estadoDeAnimo?: string;
    declare sed?: boolean;
    declare sueno?: boolean;
    declare apetito?: boolean;
    declare orina?: string;
    declare deposiciones?: string;
    declare fiebre15dias?: string;
    declare tos15dias?: string;
    declare secrecionGenitales?: string;
    declare perdidaPeso?: string;
    declare peso?: string;
    declare talla?: string;
    declare presion?: string;
    declare imc?: string;
    declare temperatura?: string;
    declare diagnostico?: string;
    declare tratamiento?: string;
    declare examenesAuxiliares?: string;
    declare fechaProximaCita?: Date;
    declare tiempoEnfermedad?: string;
    declare receta?: RecetaDTO;
    declare orden?: OrdenMedicaDTO;

    declare lugarAtencion?: string;
    declare doctorAtencion?: string;
}