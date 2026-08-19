import { CampoDTO } from "./campoDTO.model";
import { CatalogoDTO } from "./catalogoDTO.model";

export class ExpedienteMatrizDTO extends CampoDTO {
    numExpediente: string;
    estado: CatalogoDTO;
    numOficio: string;
    fechaOficio: Date;
    fecficioTexto: string;
    observacion: string;
    motivoIngreso: string;
    tokenFichaIdentificacion: string;
    tokenFichaIngreso: string;
    tipoCentro: string;
    expedienteDetalle: ExpedienteMatrizDetalleDTO[] = [];   
    numExpedienteJudicial: string; 
}

export class ExpedienteMatrizDetalleDTO extends CampoDTO {
    idExpedienteDetalle: number;
    tipoRegistro: CatalogoDTO;
    situacionJuridica: CatalogoDTO;
    variacionMedida: CatalogoDTO;
    tipoVariacion: CatalogoDTO;
    motivoVariacion: CatalogoDTO;
    numResolucion: string;
    fechaResolucion: Date;
    decision: string;
    tiempoMedSocEduAnios: number;
    tiempoMedSocEduMeses: number;
    tiempoMedSocEduDias: number;
    fechaInicioMedida: Date;
    fechaFinMedida: Date;
    corteJusticia: CatalogoDTO;
    instancia: CatalogoDTO;
    especialidad: CatalogoDTO;
    organoJurisdiccional: string;
    juez: string;
    secretario: string;
    sancionImpuesta: CatalogoDTO;
    montoReparacion: number;
    tipoMedSocEduImp: CatalogoDTO;
    lugarInfraccion: string;
    numJornadas: number;
    numExpediente: string;
    frecuenciaIngreso: CatalogoDTO;
    expedienteDelitos: ExpedienteMatrizDelitoDTO[];
    medidasSocioeducativas: ExpedienteMatrizMedidaDTO[];
    medidasAccesorias: ExpedienteMatrizMedidaDTO[];
    removido: boolean;
    numExpedienteJudicial: string;
}

export class ExpedienteMatrizDelitoDTO extends CampoDTO {
    idExpedienteDelito: number;
    delitoGenerico: CatalogoDTO;
    delitoEspecifico: CatalogoDTO;
    removido: boolean;
}

export class ExpedienteMatrizMedidaDTO extends CampoDTO {
    idExpedienteMedida: number;
    medida: CatalogoDTO;
    removido: boolean;
}
