package net.latinus.sistema.integral.gestion.seguridad.model.both.tras;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.tras.TrasladoAdolescente;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.flujo.InstanciaProcesoDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idTraslado"}, callSuper = true)
public class TrasladoDTO extends CamposDTO {
    private Long idTraslado;
    private String numTraslado;
    private JerarquiaDTO centroOrigen;
    private JerarquiaDTO centroDestino;
    private CatalogoDTO motivoTraslado;
    private String antecedentes;
    private String analisis;
    private String conclusiones;
    private String recomendaciones;
    private String descripcionSolicitud;
    private InstanciaProcesoDTO instanciaProcesoDTO;
    private List<TrasladoAdolescenteDTO> trasladoAdolescentes;
    private String tokenProceso;
    private String html;
    private String comentarioRechazo;
    private CatalogoDTO estadoTraslado;
    private Boolean completado;
    private String usuarioCreaTraslado;
}
