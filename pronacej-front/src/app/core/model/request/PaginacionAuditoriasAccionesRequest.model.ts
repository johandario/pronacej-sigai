import { PaginacionFechaRequest } from "./PaginacionFechaRequest.model";

export class PaginacionAuditoriasAccionesRequest extends PaginacionFechaRequest {
    declare userName: string;
    declare tokenIdentificadorRol: string;
    declare tokenIdentificadorAccion: string;
    declare tokenIdentificadorMenu: string;

    emitirReporte = false;
}