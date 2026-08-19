package net.latinus.sistema.integral.gestion.seguridad.repository.ia;

import net.latinus.sistema.integral.gestion.seguridad.entities.fuga.EventoFuga;
import net.latinus.sistema.integral.gestion.seguridad.entities.ia.ActaExternamiento;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.InformePermisoSalidaAdolescente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActaExternamientoRepository extends JpaRepository<ActaExternamiento, Long> {
    List<ActaExternamiento> findByFichaIdentificacionTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
    ActaExternamiento findByIdActaExternamientoAndRemovido(Long idActaExternamiento, Boolean removido);
    ActaExternamiento findByTokenIdentificadorAndRemovido(String tokenIdentificador, Boolean removido);
}
