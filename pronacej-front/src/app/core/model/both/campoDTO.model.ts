export class CampoDTO {
    esEdicion?: boolean = false;
    controlesMap?: Record<string, boolean>;
    declare tokenIdentificador?: string;
    declare tokenIdentificadorEmpresa?: string;

    declare fechaCreacion?: Date;
    declare ipCrea?: string;

    declare nombreUsuarioCrea?: string;
    declare nombreUsuarioEdita?: string;
    declare nombreUsuarioElimina?: string;
}