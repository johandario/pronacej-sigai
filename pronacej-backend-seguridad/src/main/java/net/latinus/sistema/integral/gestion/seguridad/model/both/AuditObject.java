package net.latinus.sistema.integral.gestion.seguridad.model.both;

import lombok.Builder;
import lombok.Value;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;

import java.util.Date;

@Value
@Builder
public class AuditObject {
    @Builder.Default
    Date fecha = new Date(System.currentTimeMillis());

    String ip;
    UsuarioSistema usuarioSistema;
    Empresa empresa;
}
