package net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDTO extends CamposDTO implements Serializable {
    private String id;
    private String title;
    private String subtitle;
    private String type;
    private String icon;
    private List<MenuDTO> children;
    private String tokenIdentificadorPadre;
    private String link;
    private Boolean mostrarEnFront = true;
    private String tooltip;
    private String nemonico;
    private Boolean realizaAuditoria;
    private Boolean mostrarAccionesPermisos;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
