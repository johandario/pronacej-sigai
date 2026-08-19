package net.latinus.sistema.integral.gestion.seguridad.repository.param;

import net.latinus.sistema.integral.gestion.seguridad.entities.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    Alerta findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    List<Alerta> findByCentroTokenIdentificadorAndEmpresaIdEmpresaAndRemovido(String tokenIdentificador, Long idEmpresa, Boolean removido);

    List<Alerta> findByCentroTokenIdentificadorAndEmpresaIdEmpresaAndActivoAndRemovido(String tokenIdentificador, Long idEmpresa, Boolean activo, Boolean removido);
}
