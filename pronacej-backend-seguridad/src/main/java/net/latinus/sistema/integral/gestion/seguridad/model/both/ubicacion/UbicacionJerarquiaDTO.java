package net.latinus.sistema.integral.gestion.seguridad.model.both.ubicacion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class UbicacionJerarquiaDTO extends CamposDTO {
    private UbicacionJerarquiaDTO ubicacionJerarquiaPadre;
    private JerarquiaDTO jerarquiaTipo;
    private JerarquiaDTO jerarquiaCentro;
    private String nombre;
    private String nombreCorto;
    private String descripcion;
    private CatalogoDTO tipoSexo;
    private CatalogoDTO atencionPrioritaria;
    private CatalogoDTO tipoUbicacion;
    private Long rangoInicio;
    private Long rangoFin;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}
