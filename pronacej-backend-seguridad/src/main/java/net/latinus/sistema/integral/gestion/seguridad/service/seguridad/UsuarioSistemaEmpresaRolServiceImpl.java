package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistemaEmpresaRol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.DatosDeSeguridadDeUsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.UsuarioSistemaEmpresaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.RolRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.UsuarioSistemaEmpresaRolRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.UsuarioSistemaRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioSistemaEmpresaRolServiceImpl implements UsuarioSistemaEmpresaRolService {


    private RolRepository rolRepository;

    private EmpresaRepository empresaRepository;

    private UsuarioSistemaRepository usuarioSistemaRepository;

    private JwtProviderService jwtProviderService;

    private UsuarioSistemaEmpresaRolRepository usuarioSistemaEmpresaRolRepository;

    @Override
    public RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> crearOActualizar(UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO,
                                                                                      String ip, UsuarioSistema userAccion) {
        RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            UsuarioSistemaDTO usuarioSistemaDTO = usuarioSistemaEmpresaRolDTO.getUsuarioSistemaDTO();

            UsuarioSistema usuarioSistema = this.usuarioSistemaRepository.findByTokenIdentificadorAndRemovido(
                    usuarioSistemaDTO.getTokenIdentificador(),
                    false
            );

            if (usuarioSistema == null) {
                df.setMensaje("El usuario no esta registrado o ya fue eliminado anteriormente");
                return df;
            }

            Empresa empresa = this.empresaRepository.findByTokenIdentificadorAndRemovido(usuarioSistemaEmpresaRolDTO.getTokenIdentificadorEmpresa(), false);
            if (empresa == null) {
                df.setMensaje("La empresa no esta registrada o ya fue eliminada anteriormente");
                return df;
            }

            RolDTO rolDTO = usuarioSistemaEmpresaRolDTO.getRolDTO();

            Rol rol = this.rolRepository.findByTokenIdentificadorAndRemovido(rolDTO.getTokenIdentificador(), false);
            if (rol == null) {
                df.setMensaje("El rol no esta registrado o fue eliminado anteriormente");
                return df;
            }

            UsuarioSistemaDTO usuarioSistemaDTO1 = usuarioSistemaEmpresaRolDTO.getUsuarioSistemaDTO();

            List<UsuarioSistemaEmpresaRol> usuarioSistemaEmpresaRolList = this.usuarioSistemaEmpresaRolRepository.
                    findByEmpresaTokenIdentificadorAndUsuarioSistemaTokenIdentificadorAndRemovido(
                            usuarioSistemaEmpresaRolDTO.getTokenIdentificadorEmpresa(),
                            usuarioSistemaDTO1.getTokenIdentificador(),
                            false
                    );

            if (!usuarioSistemaEmpresaRolList.isEmpty()) {
                df.setMensaje("Ya existe una relación entre el usuario y la empresa (recuerda que el usuario solo puede tener 1 rol por empresa)");
                return df;
            }

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol;
            if (usuarioSistemaEmpresaRolDTO.getEsEdicion()) {
                usuarioSistemaEmpresaRol = this.usuarioSistemaEmpresaRolRepository.findByTokenIdentificadorAndRemovido(
                        usuarioSistemaEmpresaRolDTO.getTokenIdentificador(), false
                );

                if (usuarioSistemaEmpresaRol == null) {
                    df.setMensaje("La relación a editar no existe o fue eliminada anteriormente");
                    return df;
                }

                usuarioSistemaEmpresaRol.setIpEdita(ip);
                usuarioSistemaEmpresaRol.setUsuarioSistemaEdita(userAccion);
                usuarioSistemaEmpresaRol.setFechaEdicion(new Date());

            } else {
                usuarioSistemaEmpresaRol = new UsuarioSistemaEmpresaRol();
                usuarioSistemaEmpresaRol.setUsuarioSistemaCrea(userAccion);
                usuarioSistemaEmpresaRol.setIpCrea(ip);
            }

            usuarioSistemaEmpresaRol.setEmpresa(empresa);
            usuarioSistemaEmpresaRol.setRol(rol);
            usuarioSistemaEmpresaRol.setUsuarioSistema(usuarioSistema);
            usuarioSistemaEmpresaRol = this.usuarioSistemaEmpresaRolRepository.save(usuarioSistemaEmpresaRol);

            usuarioSistemaEmpresaRolDTO.setTokenIdentificador(usuarioSistemaEmpresaRol.getTokenIdentificador());

            df.llenarRespuestaExitosa("Relación creada con éxito", usuarioSistemaEmpresaRolDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> crearUsuarioDirecto(HttpServletRequest httpServletRequest,
                                                                                         UsuarioSistemaEmpresaRolDTO usuarioSistemaEmpresaRolDTO) {
        RespuestaPorDefectoAuditoria<UsuarioSistemaEmpresaRolDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            df = this.crearOActualizar(usuarioSistemaEmpresaRolDTO, httpServletRequest.getRemoteAddr(), null);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<DatosDeSeguridadDeUsuarioSistemaDTO> obtenerDataDeSeguridad(HttpServletRequest httpServletRequest) {

        RespuestaPorDefectoAuditoria<DatosDeSeguridadDeUsuarioSistemaDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                df.setMensajeErrorReal(df2.getMensajeErrorReal());
                return df;
            }

            UsuarioSistemaEmpresaRol usuarioSistemaEmpresaRol = df2.getData().getUsuarioSistemaEmpresaRol();

            Rol rol = usuarioSistemaEmpresaRol.getRol();

            if(rol == null){
                df.setMensaje("El usuario no tiene un rol válido");
                return df;
            }

            DatosDeSeguridadDeUsuarioSistemaDTO datosDeSeguridadDeUsuarioSistemaDTO = new DatosDeSeguridadDeUsuarioSistemaDTO();
            datosDeSeguridadDeUsuarioSistemaDTO.setCambioDeContraseniaCadaNDias(
                    usuarioSistemaEmpresaRol.getCambioContraseniaCadaNDias()
            );
            datosDeSeguridadDeUsuarioSistemaDTO.setHabilitar2DoFactorDeAutenticacion(
                    usuarioSistemaEmpresaRol.getAutenticacionEn2Pasos());
            datosDeSeguridadDeUsuarioSistemaDTO.setTokenIdentificador(usuarioSistemaEmpresaRol.getTokenIdentificador());

            UsuarioSistema usuarioSistema = df2.getData().getUsuarioSistema();
            datosDeSeguridadDeUsuarioSistemaDTO.setTokenIdentificadorDeUsuarioSistema(usuarioSistema.getTokenIdentificador());

            datosDeSeguridadDeUsuarioSistemaDTO.setDiasExpiracionContrasenia(rol.getDiasExpiracionPassword());

            df.llenarRespuestaExitosa("Se obtenieron con exito los datos de seguridad del usuario: "
                    + usuarioSistema.getNombres(), datosDeSeguridadDeUsuarioSistemaDTO);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


}
