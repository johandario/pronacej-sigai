package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FichaIdentificacionResumenDTO {
    private String nombreCompleto;
    private String numeroIdentificacion;
    private String centro;
    private String estado;
    private String tokenIdentificador;
}
