package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.seguridad.RolDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.EmpresaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.RolRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class RolServiceImpl implements RolService {

    private RolRepository rolRepository;

    private JwtProviderService jwtProviderService;

    private EmpresaRepository empresaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<RolDTO> crearOEditarRol(RolDTO rolDTO, UsuarioSistema usuarioQueCrea, String ipQueCrea) {
        RespuestaPorDefectoAuditoria<RolDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<Boolean> df2 = rolDTO.chequearValoresRequeridos();
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Rol rol;
            if (rolDTO.getEsEdicion()) {
                rol = this.rolRepository.findByTokenIdentificadorAndRemovido(rolDTO.getTokenIdentificador(), false);
                if (rol == null) {
                    df.setMensaje("El rol a editar no existe o ya fue eliminado anteriormente");
                    return df;
                }

                rol.setFechaEdicion(new Date());
                rol.setIpEdita(ipQueCrea);
                rol.setUsuarioSistemaEdita(usuarioQueCrea);
            } else {
                rol = new Rol();
                rol.setFechaCreacion(new Date());
                rol.setIpCrea(ipQueCrea);
                rol.setUsuarioSistemaCrea(usuarioQueCrea);
            }            
            rol.setEsSuperRol((rolDTO.getEsSuperRol()!=null && rolDTO.getEsSuperRol()==true));
            rol.setCodigo(rolDTO.getCodigo());
            rol.setEmpresa(this.empresaRepository.findByTokenIdentificadorAndRemovido(
                    rolDTO.getTokenIdentificadorEmpresa(), false
            ));
            rol.setDescripcion(rolDTO.getDescripcion());
            rol.setEsRolPorDefecto((rolDTO.getEsRolPorDefecto()!=null && rolDTO.getEsRolPorDefecto()==true));
            rol.setDiasExpiracionPassword(rolDTO.getDiasExpiracionPassword());
            rol.setNombre(rolDTO.getNombre());

            rol = this.rolRepository.save(rol);
            rolDTO.setTokenIdentificador(rol.getTokenIdentificador());

            df.llenarRespuestaExitosa("Se realizo la operación con exito sobre el rol: " +
                    rol.getNombre(), rolDTO);

        } catch (Exception ex) {

        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<RolDTO> crearRolDirecto(HttpServletRequest httpServletRequest, RolDTO rolDTO) {
        RespuestaPorDefectoAuditoria<RolDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<Boolean> df2 = this.jwtProviderService.verificarConsumoDirecto(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            df = this.crearOEditarRol(rolDTO, null, httpServletRequest.getRemoteAddr());

        } catch (Exception ex) {

        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<RolDTO>> obtenerRolesDeEmpresa(HttpServletRequest httpServletRequest) {
        RespuestaPorDefectoAuditoria<List<RolDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            Empresa empresa = df2.getData().getEmpresa();

            List<Rol> rolList = this.rolRepository.findByEmpresaTokenIdentificadorAndRemovidoOrderByIdRolDesc(
                    empresa.getTokenIdentificador(), false
            );

            List<RolDTO> rolDTOList = new ArrayList<>();
            for (Rol rol : rolList) {
                RolDTO rolDTO = new RolDTO();
                rolDTO.setTokenIdentificador(rol.getTokenIdentificador());
                rolDTO.setCodigo(rol.getCodigo());
                rolDTO.setDescripcion(rol.getDescripcion());
                rolDTO.setNombre(rol.getNombre());
                rolDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
                rolDTOList.add(rolDTO);
            }

            df.llenarRespuestaExitosa("Se han encontrado un total de: " +
                    rolDTOList.size() + " de la empresa: " + empresa.getNombre(), rolDTOList);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }


}
