import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class PlanTratamientoIndDTO extends CampoDTO {
  idPlanTratamiento: number;  
  nombreEstado: string;
  estado: CatalogoDTO;
  instTecnicas: string;
  especFactores: PlanTratamientoIndEspecifDTO[] = [];
  ejecMedidas: PlanTratamientoIndEspecifDTO[] = [];
  unidadReceptora: PlanTratamientoIndEspecifDTO[] = [];
  factRiesgoNoCrimin: string;
  valRiesgo: string;
  reajuste?: string;
  hipotExplicativa: string;
  intensidadIntervTrat: string;
  tipoCentro: string;
  tipoAbierto: string;
  intervObjetivos: PlanTratamientoIndIntervDTO[] = [];
  intervNoCriminogenos: PlanTratamientoIndIntervDTO[] = [];
  intervDiferenciada: PlanTratamientoIndIntervDTO[] = [];
  intervMedidas: PlanTratamientoIndIntervDTO[] = [];
  tokenPadre: string;
  idFichaIdentificacion: number;
  completada: boolean;
  esActivo: boolean;
  medidasAccesorias: CatalogoDTO[] = [];
  tokenExpedienteMatrizDetalle: string;
}

export class PlanTratamientoIndEspecifDTO {
  idPlanTratIndEspecif: string;
  dimension: CatalogoDTO;
  factorRiesgo: string;
  factorProtector: string;
  comentario: string;
}

export class PlanTratamientoIndIntervDTO extends CampoDTO {
  idPlanTratIndInterv: number;
  version: string;  
  reajuste: boolean;
  activo: boolean;
  fundamentacionReajuste: string;
  fechaReajuste: Date;
  dimension: CatalogoDTO;
  objetivo: string;
  actividadPrograma: string;
  equipoResponsable: string;
  tiempoEstimado: string;
  numAtencionIndividual: string;
  numAtencionGrupal: string;
  lugar: string;
  modalidad: CatalogoDTO;
  frecuencia: CatalogoDTO;
  descripcion: string;
  fechaInicio: Date;
  fechaFin: Date;
  tokenFichaIdentificacion: string;
}

export class PlanTratamientoSeguimientoDTO extends CampoDTO {
  actividad: PlanTratamientoIndIntervDTO;
  fecha: Date;
  horaInicio: string;
  horaFin: string;
  observaciones: string;
}

export class ActividadIntervencionDTO extends CampoDTO {
  idActividadIntervencion?: number;
  idPlanTratIndInterv?: number;
  subactividad?: string;
  frecuencia?: CatalogoDTO;
  fechaInicio?: Date;
  fechaFin?: Date;
  activo?: boolean = false;
}

export class ActividadIntervencionSeguimientoDTO extends CampoDTO {
  idActividadIntervencionSeguimiento?: number;
  idActividadIntervencion?: number;
  fecha?: Date;
  horaInicio?: string;
  horaFin?: string;
  observaciones?: string;
}

export class PlanTratamientoIndSeguiDTO extends CampoDTO {
  idPlanTratamientoIndSegui: number;    
  programa: string;
  resumen: string;
  estadoSalud: string;
  observaciones: string;
  recomendaciones: string;
  fechaInicio: Date;
  fechaFin: Date;
  fecInicio: string;
  fecFin: string;
  periodoTiempo: CatalogoDTO;
  intervObjetivos: PlanTratamientoIndSeguiDetalleDTO[] = [];
  intervNoCriminogenos: PlanTratamientoIndSeguiDetalleDTO[] = [];
  intervDiferenciada: PlanTratamientoIndSeguiDetalleDTO[] = [];
  intervMedidas: PlanTratamientoIndSeguiDetalleDTO[] = [];
  tokenPadre: string;
  idPlanTratamiento: number;
}

export class PlanTratamientoIndSeguiDetalleDTO extends CampoDTO {
  idPlanTratamientoIndSeguiDetalle?: number;
  planTratamientoIndInterv?: PlanTratamientoIndIntervDTO;
  frecuencia?: CatalogoDTO;
  frecuenciaParticipacion?: CatalogoDTO;
  situacionActual?: CatalogoDTO;
  actitud?: CatalogoDTO;
  aprovechamiento?: CatalogoDTO;
  fechaInicio?: Date;
  fechaFin?: Date;
  observaciones?: string;
  indicadorDeficiente?: boolean;
  indicadorEnProceso?: boolean;
  indicadorLogrado?: boolean;
  analisis?: string;
}

export class PlanTratamientoIndSeguiAbiertoDTO extends CampoDTO {
  idPlanTratamientoIndSeguiAbierto: number;
  fecha: Date;
  hora: string;
  descripcion: string;
  tokenPtiInterv: string;
}

export class CatalogoSimpleDTO {
  idCatalogo?: number;
  tokenIdentificador?: string;
  nemonico?: string;
}