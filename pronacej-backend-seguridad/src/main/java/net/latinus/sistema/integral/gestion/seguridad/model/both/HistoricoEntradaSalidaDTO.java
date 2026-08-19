package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.fuga.EventoFugaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ActaExternamientoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.InformePermisoSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.salida.RegistroSalidaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoAdolescenteDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.tras.TrasladoDTO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HistoricoEntradaSalidaDTO extends CamposDTO{

    private Date fechaSalida;
    private JerarquiaDTO centroSalida;
    private JerarquiaDTO centroIngreso;
    private EventoFugaDTO fuga;
    private TrasladoDTO traslado;
    private InformePermisoSalidaDTO permisoSalida;
    private CatalogoDTO motivoSalida;
    private RegistroSalidaDTO registroSalida;
    private TrasladoAdolescenteDTO trasladoAdolescente;
    private InformeFinalAbiertoDTO informeFinalAbierto;
    private ActaExternamientoDTO actaExternamiento;

}
