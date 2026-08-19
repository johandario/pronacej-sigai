package net.latinus.sistema.integral.gestion.seguridad.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.EmpresaDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@Data
public class LoginResponse implements Serializable {

    private String estado;
    private String jwt;

    private String nombreRol;
    private String nombreEmpresa;
    private UserDataResponse userDataResponse;
    private String tokenIdentificadorEmpresa;
    private List<JerarquiaDTO> listaJerarquias;
    private String tokenIdentificadorJerarquia;
    private String tokenIdentificadorRolJerarquia;

    //private List<MenuDTO> menu;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
