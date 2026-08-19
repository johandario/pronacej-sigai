package net.latinus.sistema.integral.gestion.seguridad.repository.institucion;

import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.SeguimientoAdolescInst;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeguimientoAdolescInsRepository extends JpaRepository<SeguimientoAdolescInst, Long>{


    List<SeguimientoAdolescInst> findAllByRemovido(Boolean removido);
    Page<SeguimientoAdolescInst> findByRemovido(Boolean removido, Pageable pageable);

    SeguimientoAdolescInst findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);

    // Buscar todos los seguimientos de un adolescente específico por tokenIdentificador
    @Query("SELECT s FROM SeguimientoAdolescInst s " +
            "WHERE s.adolescenteDerivadoInst.tokenIdentificador = :tokenIdentificador " +
            "AND s.removido = false")
    Page<SeguimientoAdolescInst> findAllByAdolescenteTokenIdentificador(
            @Param("tokenIdentificador") String tokenIdentificador,
            Pageable pageable
    );

    // Buscar seguimientos de un adolescente con un filtro adicional
    @Query("SELECT s FROM SeguimientoAdolescInst s " +
            "WHERE s.adolescenteDerivadoInst.tokenIdentificador = :tokenIdentificador " +
            "AND s.removido = false " +
            "AND (" +
            "LOWER(s.medioEntrevista) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.resultadoEntrevista) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.recomendacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR CAST(s.fechaSeguimiento AS string) LIKE CONCAT('%', :filter, '%')" +
            ")")
    Page<SeguimientoAdolescInst> buscarPorAdolescenteYFiltro(
            @Param("tokenIdentificador") String tokenIdentificador,
            @Param("filter") String filter,
            Pageable pageable
    );

    // Búsqueda general con filtro sin importar adolescente
    @Query("SELECT s FROM SeguimientoAdolescInst s " +
            "WHERE s.removido = false " +
            "AND (" +
            "LOWER(s.medioEntrevista) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.resultadoEntrevista) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(s.recomendacion) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR CAST(s.fechaSeguimiento AS string) LIKE CONCAT('%', :filter, '%')" +
            ")")
    Page<SeguimientoAdolescInst> buscarPorFiltro(@Param("filter") String filter, Pageable pageable);
}
