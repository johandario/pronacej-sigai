import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";
import { JerarquiaDTO } from "./jerarquiaDTO.model";

export class RegistroInstitucionDTO extends CampoDTO{

    idRegistroInstitucion: number;
    nombreOrganizacion: string;
    nombreDirector: string;
    ruc: string; 
    nombContactoOperacional: string;
    direccion: string;
    telefono: string;
    fax: string; 
    email: string;
    sitioWeb: string;
    dni: string;
    misionInstitucional: string; 
    objetivoInstitucional: string;
    departamento: string;
    servicios: string;
    beneficios: string; 
    horariosServicios: string;
    serviciosArticulados: string;
    areaGeografica: string; 
    participacionEspaciosLocales: string;
    otroSitioWeb: string; 
    tipoOrganizacion: CatalogoDTO; 
    tieneConvenio: boolean;
    codigoUbigeoUbicacion: string;
    tipoInstitucion: string;
    finalidadInstitucion: string;
    estado: string;
    centro?: JerarquiaDTO;

   
    
   
    
   
   
     
}