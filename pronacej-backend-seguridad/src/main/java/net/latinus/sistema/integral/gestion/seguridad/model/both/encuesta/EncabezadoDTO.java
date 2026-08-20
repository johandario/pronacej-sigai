package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idEncabezado"}, callSuper = true)
public class EncabezadoDTO extends CamposDTO {
    private Long idEncabezado;
    private String nombre;
    private String descripcion;
    private Double valorTotal;
    private String encuesta;
    private Date fechaCompletacion;
    private Boolean completada;
    private String fichaIdentificacion;
    private List<ContestacionDTO> contestaciones;
    private EvaluacionDocumentoDTO evaluacionDocumentoDTO;
    /** Token del catálogo NIVEL_RIESGO_* (Bajo/Medio/Alto). */
    private String tokenIdentificadorValoracionFinal;
    private String justificacionValoracion;
    private Date fechaValoracion;
    /** Si true, solo actualiza valoración final (revalorar). */
    private Boolean soloValoracion;
}
