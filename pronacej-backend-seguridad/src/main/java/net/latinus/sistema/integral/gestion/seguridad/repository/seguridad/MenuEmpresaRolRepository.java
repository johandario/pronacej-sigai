package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.MenuEmpresaRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuEmpresaRolRepository extends JpaRepository<MenuEmpresaRol, Long> {

    List<MenuEmpresaRol> findByEmpresaIdEmpresaAndRolIdRolAndRemovido(Long idEmpresa, Long idRol, Boolean removido);
    
    MenuEmpresaRol findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovido(Long idEmpresa, Long idRol, Long idMenu, Boolean removido);
    
    MenuEmpresaRol findByEmpresaIdEmpresaAndRolIdRolAndMenuIdMenuAndRemovidoAndBloqueado(Long idEmpresa, Long idRol, Long idMenu, Boolean removido, Boolean bloqueado);
    
    MenuEmpresaRol findByTokenIdentificador(String tokenIdentificador);
}
