package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idPertenencia"}, callSuper = true)
public class PertenenciaDTO extends CamposDTO implements Serializable {
    private Long idPertenencia;
    private CatalogoDTO estado;
    private String comentarioEgresos;
    private String comentarioIngresos;
    private String comentarioSalidaEgresos;
    private String comentarioSalidaIngresos;
    private List<PertenenciaDetalleDTO> detalleEgresos;
    private List<PertenenciaDetalleDTO> detalleIngresos;
    private List<PertenenciaDetalleDTO> detalleSalidaIngresos;
    private String tokenFichaIdentificacion;
    private String tokenFichaIngreso;
    private Long idFichaIdentificacion;
    private String numArticulosRetirados;
    private String numArticulosEntregados;
    private String numArticulosRetiradosSalida;
    private String articulosRetirados;
    private String articulosEntregados;
    private String articulosRetiradosSalida;
    private String fecCreacionTexto;

}
