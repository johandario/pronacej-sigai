package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PertenenciaDetalleDTO implements Serializable {
    private Long idPertenenciaDetalle;
    private String nombre;
    private CatalogoDTO tipo;
    private CatalogoDTO estado;
    private Integer cantidad;
    private String observacion;
}
