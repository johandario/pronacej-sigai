import { UsuarioSistemaDTO } from "./usuarioSistemaDTO.model";

export class PasswordUserSistemaDTO{

    declare password:string;

    declare passwordEncrypt:string;

    declare nuncaExpira:boolean;

    declare fechaHabilitada:Date;

    declare usuarioSistemaDTO: UsuarioSistemaDTO; 
}