package net.latinus.sistema.integral.gestion.seguridad.repository.seguridad;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaAsistenciaPostEgreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaAsistenciaPostEgresoRepository extends JpaRepository<FichaAsistenciaPostEgreso, Long> {

    FichaAsistenciaPostEgreso findByTokenIdentificadorAndRemovido(String tokenIdentificador, boolean removido);

    Page<FichaAsistenciaPostEgreso> findByPlanAsistenciaPostEgresoDetallePlanAsistenciaTokenIdentificadorAndRemovido(String tokenFichaIdentificacion, Boolean removido, Pageable pageable);


    Page<FichaAsistenciaPostEgreso> findByPlanAsistenciaPostEgresoTokenIdentificadorAndRemovido(String tokenPlanAsistenciaPostEgreso, Boolean removido, Pageable pageable);

    @Query("SELECT f FROM FichaAsistenciaPostEgreso f " +
            "LEFT JOIN f.tipoFormato t " +
            "WHERE f.removido = false " +
            "AND (:filter IS NULL OR :filter = '' OR LOWER(t.nombre) LIKE LOWER(CONCAT('%', :filter, '%')))")
    Page<FichaAsistenciaPostEgreso> buscarPorTipoFormato(
            @Param("filter") String filter,
            Pageable pageable
    );

}
