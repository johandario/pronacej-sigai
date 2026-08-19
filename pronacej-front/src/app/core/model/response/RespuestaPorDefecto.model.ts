export class RespuestaPorDefecto<T> {
    declare titulo: string;
    declare mensaje: string;
    declare mensajeError?: string;
    declare exito: boolean;
    declare data: T;
    declare logOut: boolean;
    declare sinAcceso: boolean;
    declare codigoEstado: number;
}