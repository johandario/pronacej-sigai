import { PaginacionRequest } from "../PaginacionRequest.model";

export class PlanTratamientoIndSeguiAbiertoDocumentoRequest extends PaginacionRequest {
  declare tokenIdentificadorFichaSeguimientoAbierto: string;
  declare textoBuscar: string;
}