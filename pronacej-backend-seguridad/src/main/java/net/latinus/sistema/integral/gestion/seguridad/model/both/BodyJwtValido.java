package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistemaEmpresaRol;

@Data
public class BodyJwtValido {

    private Rol rol;
    private Empresa empresa;
    private UsuarioSistema usuarioSistema;
    private String jwt;

    private UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol;

    private Rol rolJerarquia;
    private Jerarquia jerarquia;

    private String nemonicoMenu;
}
