package net.latinus.sistema.integral.gestion.seguridad.repository.reporte;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.HistoricoFichaIdentificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportInfoAdolescentesRepository extends JpaRepository<HistoricoFichaIdentificacion, Long>,
        ExportInfoAdolescentesRepositoryCustom {
}

