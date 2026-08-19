import { CampoDTO } from "../both/campoDTO.model";
import { CatalogoDTO } from "../both/catalogoDTO.model";


export class HistoricoEntradaSalidaRequest extends CampoDTO{
    
    declare tipoEntrada: CatalogoDTO;

}