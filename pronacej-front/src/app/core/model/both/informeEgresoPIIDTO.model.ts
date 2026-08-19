import { CampoDTO } from "./campoDTO.model";

export class InformeEgresoPIIDTO extends CampoDTO {
    // Identificadores de relaciones
    declare tokenIdentificadorFichaIdentificacion: string;
    declare tokenIdentificadorInformeSeguimientoPII: string;

    // Campos de motivo de ingreso
    declare motivoIngresoPII: string;

    // Campos de descripción del plan de tratamiento
    declare descripcionPsicologicaPlanTratamiento: string;
    declare descripcionSocialPlanTratamiento: string;
    declare descripcionConductualPlanTratamiento: string;
    declare descripcionFamiliarPlanTratamiento: string;
    declare descripcionNivelRiesgoPlanTratamiento: string;

    // Campos de evolución del plan de tratamiento
    declare descripcionEvolucionPsicologicaPlanTratamiento: string;
    declare descripcionEvolucionSocialPlanTratamiento: string;
    declare descripcionEvolucionConductualPlanTratamiento: string;
    declare descripcionEvolucionFamiliarPlanTratamiento: string;
    declare descripcionEvolucionNivelRiesgoPlanTratamiento: string;

    // Campos de conclusiones y recomendaciones
    declare conclusiones: string;
    declare recomendaciones: string;

    // Campos adicionales para manejo de usuario y visualización
    declare nombreCompletoUsuarioCreacion: string;
    declare esVisualizacion?: boolean;
}