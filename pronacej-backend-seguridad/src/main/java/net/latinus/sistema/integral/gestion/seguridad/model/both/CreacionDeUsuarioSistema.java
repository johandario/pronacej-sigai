package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreacionDeUsuarioSistema extends UsuarioSistemaDTO {

    private String tokenIdentificadorRol;
    private String nombreRol;

    private String tokenRelacion;
    private Boolean bloqueadoRelacion = false;


    public UsuarioSistemaDTO obtenerUsuarioSistemaDTO() {
        UsuarioSistemaDTO usuarioSistemaDTO = new UsuarioSistemaDTO();
        usuarioSistemaDTO.setTokenIdentificador(this.getTokenIdentificador());
        usuarioSistemaDTO.setNombres(this.getNombres());
        usuarioSistemaDTO.setApellidos(this.getApellidos());
        usuarioSistemaDTO.setLogo(this.getLogo());
        usuarioSistemaDTO.setEmail(this.getEmail());
        usuarioSistemaDTO.setPassword(this.getPassword());
        usuarioSistemaDTO.setTelefono(this.getTelefono());
        usuarioSistemaDTO.setNumeroDeCelular(this.getNumeroDeCelular());
        usuarioSistemaDTO.setTokenIdentificadorTipoDeDocumento(this.getTokenIdentificadorTipoDeDocumento());
        usuarioSistemaDTO.setNumeroDeDocumento(this.getNumeroDeDocumento());
        usuarioSistemaDTO.setUserName(this.getUserName());
        usuarioSistemaDTO.setEsEdicion(this.getEsEdicion());
        usuarioSistemaDTO.setTokenIdentificadorEmpresa(this.getTokenIdentificadorEmpresa());
        usuarioSistemaDTO.setAsignaciones(this.getAsignaciones());

        return usuarioSistemaDTO;
    }

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }

}
