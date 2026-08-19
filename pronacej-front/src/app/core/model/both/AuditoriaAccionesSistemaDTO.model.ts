import { AuditoriaServicioRestDTO } from "./AuditoriaServicioRestDTO.model";
import { CampoDTO } from "./campoDTO.model";

export class AuditoriaAccionesSistemaDTO extends CampoDTO {

    declare fechaFinAccion: Date;
    declare fechaInicioAccion: Date;
    declare tokenIdentificadorAccion: string;
    declare nombreAccion: string;
    declare auditoriaServicioRestDTO: AuditoriaServicioRestDTO;

    declare tokenIdentificadorMenu: string;
    declare nombreMenu: string;

    declare tokenIdentificadorRol: string;
    declare nombreRol: string;

    declare tokenIdentificadorUsuarioQueRealizaLaAccion: string;
    declare nombreUsuarioQueRealizaLaAccion: string;
    declare userNameUsuarioQueRealizaLaAccion: string;
    declare emailUsuarioQueRealizaLaAccion: string;

    declare descripcion: string;

    declare modulo: string;
}