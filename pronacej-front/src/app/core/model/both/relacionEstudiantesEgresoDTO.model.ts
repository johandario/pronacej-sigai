import { CampoDTO } from "./campoDTO.model";

export class RelacionAdolescentesEgresoDTO extends CampoDTO {
   // Datos de la medida socioeducativa  
   declare fechaInicioMedida: Date;
   declare tiempoPermanencia: number;
   declare tokenIdentificadorUnidadTiempo: string;
   declare fechaTerminoMedida: Date;
   
   // Datos de preparación para egreso
   declare fechaInicioPreparacionEgreso: Date; 
   declare diasPreparacion: number;
   declare esGraciaPresidencial: boolean;
   
   // Campos adicionales de control
   declare tokenIdentificadorFichaIdentificacion: string;
   declare esVisualizacion?: boolean;
}