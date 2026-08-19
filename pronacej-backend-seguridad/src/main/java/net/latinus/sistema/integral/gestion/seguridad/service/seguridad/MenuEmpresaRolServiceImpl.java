package net.latinus.sistema.integral.gestion.seguridad.service.seguridad;

import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Menu;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Rol;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.MenuEmpresaRol;
import net.latinus.sistema.integral.gestion.seguridad.model.both.MenuEmpresaRolDTO;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.MenuEmpresaRolRepository;

@Service
@AllArgsConstructor
public class MenuEmpresaRolServiceImpl implements MenuEmpresaRolService {

    private MenuEmpresaRolRepository menuEmpresaRolRepository;

    private JwtProviderService jwtProviderService;

    @Override
    public RespuestaPorDefectoAuditoria<MenuEmpresaRolDTO> crearMenuEmpresaRol(Empresa empresa, Rol rol, Menu menu, UsuarioSistema usuarioQueCrea, String ip) {
        RespuestaPorDefectoAuditoria<MenuEmpresaRolDTO> df = new RespuestaPorDefectoAuditoria<>();
        
        try {
            MenuEmpresaRol menuEmpresaRol = new MenuEmpresaRol();
            menuEmpresaRol.setFechaCreacion(new Date());
            menuEmpresaRol.setUsuarioSistemaCrea(usuarioQueCrea);
            menuEmpresaRol.setIpCrea(ip);
            menuEmpresaRol.setBloqueado(Boolean.FALSE);
            menuEmpresaRol.setRemovido(Boolean.FALSE);
            menuEmpresaRol.setEmpresa(empresa);
            menuEmpresaRol.setRol(rol);
            menuEmpresaRol.setMenu(menu);
            menuEmpresaRol = menuEmpresaRolRepository.save(menuEmpresaRol);
            
            MenuEmpresaRolDTO menuEmpresaRolDTO = new MenuEmpresaRolDTO();
            menuEmpresaRolDTO.setId(menuEmpresaRol.getTokenIdentificador());
            menuEmpresaRolDTO.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());
            menuEmpresaRolDTO.setTokenIdentificadorRol(rol.getTokenIdentificador());
            menuEmpresaRolDTO.setTokenIdentificadorMenu(menu.getTokenIdentificador());
            menuEmpresaRolDTO.setFechaCreacion(menuEmpresaRol.getFechaCreacion());
            
            df.llenarRespuestaExitosa("Se ha creado con éxito el permiso del menu " +
                    menu.getTitulo()+ " para el rol " + rol.getNombre(), menuEmpresaRolDTO);
            
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<Boolean> eliminarMenuEmpresaRol(Empresa empresa, Rol rol, Menu menu, UsuarioSistema usuarioQueCrea, String ip) {
        RespuestaPorDefectoAuditoria<Boolean> df = new RespuestaPorDefectoAuditoria<>();
        
        try {
            MenuEmpresaRol menuEmpresaRol = menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovido(empresa.getIdEmpresa(), rol.getIdRol(), menu.getIdMenu(), Boolean.FALSE);
            if(menuEmpresaRol!=null) {
                menuEmpresaRolRepository.delete(menuEmpresaRol);
                df.llenarRespuestaExitosa("Se ha eliminado con éxito el permiso del menu " +
                    menu.getTitulo()+ " para el rol " + rol.getNombre(), Boolean.TRUE);
            } else {
                df.llenarRespuestaExitosa("No se pudo eliminar el permiso porque no existe", Boolean.TRUE);
            }
            
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<MenuEmpresaRolDTO>> obtenerTodosPorEmpresaYRol(Empresa empresa, Rol rol) {
        RespuestaPorDefectoAuditoria<List<MenuEmpresaRolDTO>> df = new RespuestaPorDefectoAuditoria<>();
        
        try {
            List<MenuEmpresaRol> listaMenuEmpresaRol = menuEmpresaRolRepository.findByEmpresaIdEmpresaAndRolIdRolAndRemovido(empresa.getIdEmpresa(), rol.getIdRol(), Boolean.FALSE);
            List<MenuEmpresaRolDTO> listaMenuEmpresaRolDTO = new ArrayList<>();
            for(MenuEmpresaRol mer : listaMenuEmpresaRol) {
                MenuEmpresaRolDTO merDTO = new MenuEmpresaRolDTO();
                merDTO.setId(mer.getTokenIdentificador());
                merDTO.setTokenIdentificador(mer.getTokenIdentificador());
                merDTO.setTokenIdentificadorEmpresa(mer.getEmpresa().getTokenIdentificador());
                merDTO.setTokenIdentificadorRol(mer.getRol().getTokenIdentificador());
                merDTO.setTokenIdentificadorMenu(mer.getMenu().getTokenIdentificador());
                
                listaMenuEmpresaRolDTO.add(merDTO);
            }
            
            df.llenarRespuestaExitosa("Se obtuvieron con éxito " + listaMenuEmpresaRolDTO.size() + " permisos para el rol " + rol.getNombre(), listaMenuEmpresaRolDTO);
            
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        
        return df;
    }
    
}
