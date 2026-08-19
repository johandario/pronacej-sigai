import { UsuarioSistemaDTO } from "../../both/seguridad/usuarioSistemaDTO.model";
import { RutaContenidoCarpetaResponse } from "./RutaContenidoCarpetaResponse.model";

export class ContenidoCarpetaResponse extends RutaContenidoCarpetaResponse {

    declare tokenIdentificadorDocumento: string;
    declare tokenIdentificadorCarpeta: string;

    declare descripcion: string;

    declare fechaDeCreacion: Date;
    declare usuarioQueCreo: UsuarioSistemaDTO;
    declare sizeBytes: number;
    tipo = "carpeta";

    declare cantidadDeDocumentos: number;
    declare cantidadDeCarpetas: number;


    declare documentos: ContenidoCarpetaResponse[];
    declare carpetas: ContenidoCarpetaResponse[];


    declare rutaContenidoCarpetaResponseList: RutaContenidoCarpetaResponse[];
}