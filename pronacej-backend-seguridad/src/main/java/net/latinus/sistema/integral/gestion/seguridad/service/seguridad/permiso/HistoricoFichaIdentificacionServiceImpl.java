package net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso;

import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.HistoricoFichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AuditObject;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.ActaExternamientoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.ia.InformeFinalAbiertoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.salida.RegistroSalidaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.FichaIdentificacionRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.permiso.HistoricoFichaIdentificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HistoricoFichaIdentificacionServiceImpl implements HistoricoFichaIdentificacionService {
    private final HistoricoFichaIdentificacionRepository historicoFichaIdentificacionRepository;
    private final FichaIdentificacionRepository fichaIdentificacionRepository;
    private final JerarquiaRepository jerarquiaRepository;
    private final ActaExternamientoRepository actaExternamientoRepository;
    private final InformeFinalAbiertoRepository informeFinalAbiertoRepository;
    private final RegistroSalidaRepository registroSalidaRepository;

    @Override
    @Transactional
    public void crearActualizar(FichaIdentificacion fichaIdentificacion, String observacionIngreso, RegistroSalida registroSalida, AuditObject auditObject, boolean esFinal) {
        Long idFichaIdentificacion = fichaIdentificacion.getIdFichaIdentificacion();
        Long idCentro = fichaIdentificacion.getCentroIngreso().getIdJerarquia();

        Optional<HistoricoFichaIdentificacion> fichaOptional = this.historicoFichaIdentificacionRepository.findByFichaIdentificacionIdFichaIdentificacionAndCentroIdJerarquiaAndActivoAndRemovido(
                idFichaIdentificacion, idCentro, true, false
        );

        HistoricoFichaIdentificacion entity;
        if (fichaOptional.isEmpty()) {

            this.finalizarHistoricoPorFichaYCentro(idFichaIdentificacion, auditObject);

            entity = new HistoricoFichaIdentificacion();
            entity.setFichaIdentificacion(fichaIdentificacion);
            entity.setCentro(fichaIdentificacion.getCentroIngreso());
            entity.setTipoIngreso(fichaIdentificacion.getTipoEntrada());

            if (observacionIngreso != null) entity.setObservacionIngreso(observacionIngreso);

            entity.setFechaInicio(new java.sql.Date(System.currentTimeMillis()));
            entity.setActivo(true);

            if (registroSalida != null) entity.setRegistroSalida(registroSalida);

            entity.setUsuarioSistemaCrea(auditObject.getUsuarioSistema());
            entity.setFechaCreacion(auditObject.getFecha());
            entity.setIpCrea(auditObject.getIp());

            this.historicoFichaIdentificacionRepository.save(entity);
        } else {
            entity = fichaOptional.get();

            if (registroSalida != null) entity.setRegistroSalida(registroSalida);

            if (esFinal) entity.setFechaFin(new java.sql.Date(System.currentTimeMillis()));

            if (esFinal) entity.setActivo(false);

            entity.setUsuarioSistemaEdita(auditObject.getUsuarioSistema());
            entity.setFechaEdicion(auditObject.getFecha());
            entity.setIpEdita(auditObject.getIp());
            this.historicoFichaIdentificacionRepository.save(entity);
        }
    }

    /**
     * Finalizar registros históricos previos a crear un nuevo registro
     * @param idFichaIdentificacion id de ficha
     * @param auditObject objeto con datos de auditoria a nivel de entidad
     */
    private void finalizarHistoricoPorFichaYCentro(Long idFichaIdentificacion, AuditObject auditObject) {
        if (idFichaIdentificacion == null) return;

        List<HistoricoFichaIdentificacion> lista
                = this.historicoFichaIdentificacionRepository
                .findByFichaIdentificacionIdFichaIdentificacionAndActivoAndRemovido(
                        idFichaIdentificacion, true, false
                );

        lista.forEach(h -> {
            h.setFechaFin(new java.sql.Date(System.currentTimeMillis()));
            h.setActivo(false);
            h.setUsuarioSistemaEdita(auditObject.getUsuarioSistema());
            h.setFechaEdicion(auditObject.getFecha());
            h.setIpEdita(auditObject.getIp());
        });

        this.historicoFichaIdentificacionRepository.saveAll(lista);
    }
}
