import { CatalogoDTO } from "../catalogoDTO.model";
import { MedicamentoDTO } from "./medicamentoDTO.model";


export class DetalleRecetaDTO {
    declare tokenIdentificador?: string;
    declare medicamento?: string;
    declare dosis?: string;
    declare frecuencia?: string;
    declare indicaciones?: string;
    declare concentracion?: string;
    declare formaFarmaceutica?: CatalogoDTO;
    declare medicamentoCompleto?: MedicamentoDTO;
}
