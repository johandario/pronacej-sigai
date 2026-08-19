package net.latinus.sistema.integral.gestion.seguridad.model.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import net.latinus.sistema.integral.gestion.seguridad.model.both.CreacionDeRol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.MenuDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
public class AdministrarMenuRolRequest implements Serializable {

    private CreacionDeRol rol;
    private List<MenuDTO> listaMenus;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
