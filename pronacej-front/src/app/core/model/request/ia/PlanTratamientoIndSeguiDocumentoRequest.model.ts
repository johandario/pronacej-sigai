import { PaginacionRequest } from "../PaginacionRequest.model";

export class PlanTratamientoIndSeguiDocumentoRequest extends PaginacionRequest {
  declare tokenIdentificadorSeguimiento: string;
  declare textoBuscar: string;
}