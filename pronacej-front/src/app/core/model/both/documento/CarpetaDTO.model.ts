import { CampoDTO } from '../campoDTO.model';

export class CarpetaDTO extends CampoDTO {
    declare descripcion: string;

    declare nombreCliente: string;
    declare nombreAlfresco: string;

    declare carpetaDTOPadre: CarpetaDTO;
}
