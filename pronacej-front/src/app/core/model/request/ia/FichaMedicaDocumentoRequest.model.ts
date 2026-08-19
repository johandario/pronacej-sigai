import { PaginacionRequest } from "../PaginacionRequest.model";

export class FichaMedicaDocumentoRequest extends PaginacionRequest {
  declare tokenIdentificadorFichaMedica: string;
  declare textoBuscar: string;
}