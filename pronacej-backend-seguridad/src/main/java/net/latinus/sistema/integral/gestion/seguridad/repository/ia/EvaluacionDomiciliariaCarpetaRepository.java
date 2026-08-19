package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.EvaluacionDomiciliariaCarpeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluacionDomiciliariaCarpetaRepository extends JpaRepository<EvaluacionDomiciliariaCarpeta, Long> {
    /**
     * Busca evaluación domiciliaria carpeta por token identificador de evaluación domiciliaria y estado removido
     * 
     * @param tokenIdentificadorEvaluacionDomiciliaria Token identificador de evaluación domiciliaria
     * @param removido Estado de eliminación
     * @param pageable Información de paginación
     * 
     * @return Page con los resultados
     */
    Page<EvaluacionDomiciliariaCarpeta> findByEvaluacionDomiciliariaTokenIdentificadorAndRemovido(String tokenIdentificadorEvaluacionDomiciliaria, Boolean removido, Pageable pageable);
    
    /**
     * Busca el primer registro de evaluación domiciliaria carpeta por token identificador de evaluación domiciliaria y estado removido
     * 
     * @param tokenIdentificadorEvaluacionDomiciliaria Token identificador de evaluación domiciliaria
     * @param removido Estado de eliminación
     * 
     * @return Evaluación domiciliaria carpeta encontrada o null
     */
    EvaluacionDomiciliariaCarpeta findFirstByEvaluacionDomiciliariaTokenIdentificadorAndRemovido(String tokenIdentificadorEvaluacionDomiciliaria, Boolean removido);
}