package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.DatosFamiliaresCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosFamiliaresCarpetaRepository extends JpaRepository<DatosFamiliaresCarpeta, Long> {
    Page<DatosFamiliaresCarpeta> findByDatosFamiliaresTokenIdentificadorAndRemovido(String tokenIdentificadorDatosFamiliares, Boolean removido, Pageable pageable);
    
    DatosFamiliaresCarpeta findFirstByDatosFamiliaresTokenIdentificadorAndRemovido(String tokenIdentificadorDatosFamiliares, Boolean removido);
}