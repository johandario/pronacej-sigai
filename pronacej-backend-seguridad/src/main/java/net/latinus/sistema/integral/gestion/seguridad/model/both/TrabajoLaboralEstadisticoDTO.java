package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TrabajoLaboralEstadisticoDTO {
    private String nombreInstitucion;
    private String ruc;
    private Integer cantidad;

}