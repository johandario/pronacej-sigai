    import { CampoDTO } from "./campoDTO.model";
    import { PersonaRelacionadaDTO } from "./PersonaRelacionadaDTO.model";

    export class OrientacionConsejeriaFamiliarDTO extends CampoDTO {
        // Campos básicos de la orientación/consejería
        declare fecha: Date;
        declare descripcion: string;
        
        // Referencia a la persona relacionada
        declare tokenIdentificadorPersonaRelacionada: string;

        // Referencia al usuario registrado
        declare nombreCompletoUsuarioCreacion: string
    }