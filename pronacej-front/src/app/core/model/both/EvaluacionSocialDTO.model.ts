import { CampoDTO } from "./campoDTO.model";
import { EvaluacionSocialArtefactoDTO } from "./EvaluacionSocialArtefactoDTO.model";
import { PersonaRelacionadaDTO } from "./PersonaRelacionadaDTO.model";


export class EvaluacionSocialDTO extends CampoDTO {
    
    declare tokenIdentificadorFichaIdentificacion: string;
    declare tokenIdentificadorZonaVivienda: string;
    declare tokenIdentificadorSubZona: string;
    declare tokenIdentificadorMaterialParedVivienda: string;
    declare tokenIdentificadorMaterialPisoVivienda: string;
    declare tokenIdentificadorMaterialTechoVivienda: string;
    declare tokenIdentificadorAbastecimientoAguaVivienda: string;
    declare tokenIdentificadorTipoVivienda: string;
    declare tokenIdentificadorTipoAlumbradoVivienda: string;
    declare tokenIdentificadorCombustibleCocinarVivienda: string;
    declare tokenIdentificadorTipoDesagueVivienda: string;
    declare tokenIdentificadorTenencia: string;
    declare tokenIdentificadorOtrosServicios: string;
    
    declare listaPersonasRelacionadas: PersonaRelacionadaDTO[];
    declare listaArtefactos: EvaluacionSocialArtefactoDTO[];

    declare numeroAmbientes: string;
    declare numeroOcupantes: number;
    declare numeroHabitaciones: number;
    declare numeroDormitorios: number;
    declare grupoAmical: string;
    declare factorRiesgoMedio: string;
    declare areaAcademicoLaboral: string;
    declare areaSocialRecreacional: string;
    declare areaFamiliarPareja: string;
    declare areaPersonal: string;

    declare nombreCompletoUsuarioCreacion: string;
    declare esVisualizacion?: boolean;

}