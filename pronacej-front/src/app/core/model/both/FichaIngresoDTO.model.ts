import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";
import { DatosHijoIngresadoDTO } from "./DatosHijoIngresadoDTO.model";
import { JerarquiaDTO } from "./jerarquiaDTO.model";

export class FichaIngresoDTO extends CampoDTO {
    // Declaración de los campos generales
    declare tokenIdentificadorFichaIdentificacion: string;
    declare fechaIngreso: Date;
    declare tokenIdentificadorCentro: string;
    declare centro: JerarquiaDTO;
    declare observaciones: string;

    // Declaración de los campos abiertos
    declare responsableInscripcion: string;
    declare tokenIdentificadorTutor: string; // Catalogo por ahora
    declare caracteristicasParticulares: string;

    // Declaración de los campos cerrados
    declare atencionSalud: boolean;
    declare motivo: string;
    declare tokenIdentificadorProgramaDerivado: string; // Catalogo
    declare lesiones: boolean;
    declare especificarZonaLesiones: string;
    declare moretones: boolean;
    declare especificarZonaMoretones: string;
    declare cicatrices: boolean;
    declare especificarZonaCicatrices: string;
    declare tatuajes: boolean;
    declare especificarZonaTatuajes: string;
    declare piercing: boolean;
    declare especificarZonaPiercing: string;
    declare otros: boolean;
    declare especificarZonaOtros: string;
    declare victimaAgresion: boolean;
    declare especificarAgresion: string;
    declare tokenIdentificadorSeguroSalud: string; // Catalogo
    declare tokenIdentificadorFormaCabeza: string; // Catalogo
    declare tokenIdentificadorFormaNariz: string; // Catalogo
    declare tokenIdentificadorFormaLabios: string; // Catalogo
    declare tokenIdentificadorFormaCuerpo: string; // Catalogo
    declare tokenIdentificadorAnomaliaOjos: string; // Catalogo
    declare esEmbarazada: boolean;
    declare mesesEmbarazo: number;
    declare ingresaConHijo: boolean;

    declare datosHijoIngresado?: DatosHijoIngresadoDTO;
    
    // Declaración de otros campos
    declare esEdicion: boolean;
    declare esVisualizacion?: boolean;

    declare tokenIdentificadorCarpeta?: string;

    declare nombreSeguro: string;
    declare estadoAdolescente: CatalogoDTO;	
    
}