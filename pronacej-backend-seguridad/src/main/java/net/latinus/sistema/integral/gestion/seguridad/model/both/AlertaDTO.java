package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlertaDTO extends CamposDTO {
    private Long idAlerta;
    private String descripcion;
    private String mensaje;
    private String ruta;
    private String tabla;
    private String campo;
    private String prioridad;
    private String unidadTiempo;
    private Long tiempo;
    private Boolean activo;
    private String tokenFichaIdentificacion;
    private String tokenCentro;
    private String nombreCentro;
    private String nombresAdolescente;
    private String apellidoPaternoAdolescente;
    private String apellidoMaternoAdolescente;

    public AlertaDTO(Long idAlerta, String descripcion, String mensaje, String ruta, String prioridad, String tokenFichaIdentificacion, String nombresAdolescente, String apellidoPaternoAdolescente, String apellidoMaternoAdolescente) {
        this.idAlerta = idAlerta;
        this.descripcion = descripcion;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.prioridad = prioridad;
        this.tokenFichaIdentificacion = tokenFichaIdentificacion;
        this.nombresAdolescente = nombresAdolescente;
        this.apellidoPaternoAdolescente = apellidoPaternoAdolescente;
        this.apellidoMaternoAdolescente = apellidoMaternoAdolescente;
    }
}
