package net.latinus.sistema.integral.gestion.seguridad.model.both;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreacionDeRol extends RolDTO {

    private String tokenRelacion;
    private Boolean bloqueadoRelacion = false;


    public RolDTO obtenerRolDTO() {
        RolDTO rolDTO = new RolDTO();
        rolDTO.setTokenIdentificador(this.getTokenIdentificador());
        rolDTO.setCodigo(this.getCodigo());
        rolDTO.setNombre(this.getNombre());
        rolDTO.setDescripcion(this.getDescripcion());
        rolDTO.setDiasExpiracionPassword(this.getDiasExpiracionPassword());
        rolDTO.setEsSuperRol(this.getEsSuperRol());
        rolDTO.setEsRolPorDefecto(this.getEsRolPorDefecto());
        rolDTO.setEsEdicion(this.getEsEdicion());
        rolDTO.setTokenIdentificadorEmpresa(this.getTokenIdentificadorEmpresa());

        return rolDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
