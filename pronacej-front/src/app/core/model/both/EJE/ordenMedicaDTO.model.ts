import { EspecialidadProductoDTO } from "./especialidadProductoDTO.model";

export class OrdenMedicaDTO {
    declare tokenIdentificador?: string;
    declare numeroOrden?: string;
    declare fechaEmision?: Date;
    declare observaciones?: string;
    declare detalles?: OrdenMedicaDetalleDTO[];
    declare tokenIdConsultaAtencionIntegral?: string;
}

export class OrdenMedicaDetalleDTO {
    declare tokenIdentificador?: string;
    declare especialidadProducto?: EspecialidadProductoDTO;
}