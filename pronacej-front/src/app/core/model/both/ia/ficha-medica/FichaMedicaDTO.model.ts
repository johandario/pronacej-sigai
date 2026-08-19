import { List } from "lodash";
import { CatalogoDTO } from "../../catalogoDTO.model";
import { PersonaRelacionadaEnfermedadDTO } from "../../personaRelacionadaEnfermedadDTO.model";
import { FichaMedicaEnfermedadDTO } from "../../fichaMedicaEnfermedadDTO.model";

export class FichaMedicaDTO{

    declare tokenIdentificador?: string;
    declare tokenIdFichaIdentificacion: string;
    // declare lesiones: string;
    // declare enfermedades: string;
    // declare medicamentos: string;
    // declare seguroMedico: string;
    // declare institucionAcude: string;
    // declare internadoHospital: string;
    declare estadoSalud: string;

    declare tipoSangre: CatalogoDTO;

    declare enfermedadesPersonasRelacionada: PersonaRelacionadaEnfermedadDTO[];
    declare tokensEnfermedadEliminar: string[];

    declare enfermedadesRelacionadas: FichaMedicaEnfermedadDTO[];
    declare tokensEnfermedadesFichaEliminar: string[];

    declare alergiaMedicamentos: boolean;
    declare medicamentosAlergicos: string;
    declare alergiaAlimentos: boolean;
    declare detalleAlergiasAlimentos: string;
    declare cirugiaQuirurgica: boolean;
    declare detalleCirugias: string;
    declare fracturas: boolean;
    declare detalleFracturas: string;
    declare irs: string;
    declare usoDePreservativo: boolean;

    declare relacionGenero: string;

    declare icd: string;
    declare drogaInicio: string;
    declare habitosNocivos: boolean;
    declare tomaAlcohol: boolean;
    declare tabaco: boolean;
    declare edadAlcohol: string;
    declare edadTabaco: string;

    declare peso?: string; // Detalla peso en kilogramos
    declare talla?: string; // Detalla altura en metros
    declare aspectoGeneralFisico: string; // Detalla un aspecto general físico del adolescente
    declare inspeccion: string; // Detalla una inspección física del adolescente
    declare pielFaneras: string;

    declare indiceMasaCorporal?: string;
    declare saturacionOxigeno?: string;
    declare presion?: string;

    declare cabezaDetalle: string ;
    declare ojosDetalle: string ; 
    declare oidoDetalle: string ; 
    declare narizDetalle: string ; 
    declare bocaDetalle: string ; 
    declare orofaringeDetalle: string ; 
    declare corazonDetalle: string ; 
    declare pulmonesDetalle: string ; 
    declare abdomenDetalle: string ; 
    declare urinarioDetalle: string ; 
    declare pplDetalle: string ; 
    declare pruDetalle: string ; 
    declare impresionDiagnostico: string;

}