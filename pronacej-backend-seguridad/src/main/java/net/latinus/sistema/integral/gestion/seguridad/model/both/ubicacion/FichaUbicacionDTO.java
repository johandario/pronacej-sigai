package net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class FichaUbicacionDTO extends CamposDTO {

    private String tokenIdentificadorFichaIdentificacion;
    private Date fechaIngreso;
    private Boolean ubicacionActual;
    private UbicacionJerarquiaDTO ubicacionJerarquia;
    private JerarquiaDTO centro;
    private Long numeroCama;
    private Boolean atencionPrioritaria;
    private Boolean ingresoExpediente;
    private String observaciones;

    private String celdaActualTexto;
    private String centroActualTexto;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

