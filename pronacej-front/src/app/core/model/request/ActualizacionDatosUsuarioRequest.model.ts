import { UsuarioSistemaDTO } from "../both/seguridad/usuarioSistemaDTO.model";

export class ActualizacionDatosUsuarioRequest extends UsuarioSistemaDTO {

    declare tokenRecaptchaV3: string;
}