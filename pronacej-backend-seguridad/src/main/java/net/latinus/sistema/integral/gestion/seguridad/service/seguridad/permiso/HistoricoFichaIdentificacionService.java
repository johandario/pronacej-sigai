package net.latinus.sistema.integral.gestion.seguridad.service.seguridad.permiso;

import net.latinus.sistema.integral.gestion.seguridad.entities.FichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.entities.salida.RegistroSalida;
import net.latinus.sistema.integral.gestion.seguridad.model.both.AuditObject;

public interface HistoricoFichaIdentificacionService {

    /**
     * Crear/Actualizar registro de histórico a nivel de ficha de identificación
     *
     * @param fichaIdentificacion ficha de identificación para histórico
     * @param observacionIngreso
     * @param registroSalida      token de registro de salida en caso de que exista egreso del adolescente
     * @param auditObject         objeto con datos de auditoria a nivel de entidad
     * @param esFinal             bandera para saber si es un registro final
     */
    void crearActualizar(FichaIdentificacion fichaIdentificacion, String observacionIngreso, RegistroSalida registroSalida, AuditObject auditObject, boolean esFinal);
}
