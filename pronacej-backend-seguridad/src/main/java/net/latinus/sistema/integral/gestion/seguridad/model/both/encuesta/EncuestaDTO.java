package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idEncuesta"}, callSuper = true)
public class EncuestaDTO extends CamposDTO implements Serializable {
    private long idEncuesta;
    private String nombre;
    private String descripcion;
    private Boolean seccionesOrdenadas;
    private Long idJerarquia;
    private String nemonico;
    private String nemonicoCentro;
    private String tipoCentro;
    private String nemonicoCategoria;
    private String categoria;
    private String adolescente;
    private String dniAdolescente;
    /** Cabecera informe SAVRY (datos de ficha / encabezado al cargar evaluación). */
    private Integer edadAdolescente;
    private Date fechaNacimientoAdolescente;
    private Long correlativo;
    private String establecimiento;
    private Date fechaRegistro;
    private String evaluador;
    private Date fechaEvaluacion;
    private List<SeccionDTO> secciones = new ArrayList<>();
    /** Campos de evaluación (encabezado) expuestos al cargar SAVRY/nivel de riesgo. */
    private Boolean completada;
    private String tokenIdentificadorValoracionFinal;
    private String nombreValoracionFinal;
    private String justificacionValoracion;
    private Date fechaValoracion;
}
