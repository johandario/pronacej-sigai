package net.latinus.sistema.integral.gestion.seguridad.repository.reporte;

import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.permiso.HistoricoFichaIdentificacion;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.AdolescenteExternadoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdolescenteExternadoRepository extends JpaRepository<HistoricoFichaIdentificacion, Long> {
    @Query(
            value = """
                  select
                    distinct
                    upper(trim(concat_ws(' ',
                      trim(ifi.nombres),
                      trim(ifi.apellido_paterno),
                      trim(ifi.apellido_materno)
                    ))) as nombreCompleto,
                    ifi.numero_identificacion as numeroIdentificacion,
                    sj.nombre as centro,
                    coalesce(
                      iem.num_expediente,
                      (
                        select iem2.num_expediente
                        from ia_expediente_matriz iem2
                        where iem2.id_ficha_identificacion = ifi.id_ficha_identificacion
                        and iem2.fecha_creacion between shfi.fecha_inicio  and shfi.fecha_fin
                        order by iem2.fecha_creacion desc
                        limit 1
                      )
                    ) as numeroExpediente,
                    TO_CHAR(shfi.fecha_inicio, 'DD/MM/YYYY HH24:MI:SS') as fechaIngreso,
                    TO_CHAR(shfi.fecha_fin,   'DD/MM/YYYY HH24:MI:SS') as fechaSalida,
                    --shfi.fecha_inicio as fechaIngreso,
                    --shfi.fecha_fin as fechaSalida,
                    pc2.nombre as motivoIngreso,
                    pc.nombre as motivoSalida,
                    shfi.observacion_ingreso as observacionIngreso,
                    sr.observaciones as observacionSalida
                  from seg_historico_ficha_identificacion shfi
                  join ia_ficha_identificacion ifi on ifi.id_ficha_identificacion = shfi.id_ficha_identificacion
                  join salida_registro sr on sr.id_registro_salida = shfi.id_registro_salida
                  left join ia_acta_externamiento iae on iae.id_acta_externamiento = sr.id_externamiento
                  left join ia_expediente_matriz iem on iem.id_expediente = iae.id_expediente_matriz
                  left join par_catalogo pc on pc.id_catalogo = sr.id_catalogo_motivo_salida
                  left join par_catalogo pc2 on pc2.id_catalogo = shfi.id_catalogo_tipo_ingreso
                  left join seg_jerarquia sj on sj.id_jerarquia = shfi.id_centro
                  where ((:valorCentro is null
                          or :valorCentro = ''
                          or sj.nombre ilike concat('%', :valorCentro, '%'))
                          and
                          (:valorFiltro is null
                          or :valorFiltro = ''
                          or upper(trim(concat_ws(' ',
                                trim(ifi.nombres),
                                trim(ifi.apellido_paterno),
                                trim(ifi.apellido_materno)
                              ))) ilike concat('%', :valorFiltro, '%')
                          or ifi.numero_identificacion ilike concat('%', :valorFiltro, '%')
                          or pc2.nombre ilike concat('%', :valorFiltro, '%')
                          or pc.nombre ilike concat('%', :valorFiltro, '%')
                          or shfi.observacion_ingreso ilike concat('%', :valorFiltro, '%')
                          or sr.observaciones ilike concat('%', :valorFiltro, '%')
                          -- 🔹 numeroExpediente
                                  or coalesce(
                              iem.num_expediente,
                              (
                                  select iem2.num_expediente
                                  from ia_expediente_matriz iem2
                                  where iem2.id_ficha_identificacion = ifi.id_ficha_identificacion
                                  and iem2.fecha_creacion between shfi.fecha_inicio and shfi.fecha_fin
                                  order by iem2.fecha_creacion desc
                                  limit 1
                              )
                          ) ilike concat('%', :valorFiltro, '%')
                          -- 🔹 fechaIngreso
                          or to_char(shfi.fecha_inicio, 'DD/MM/YYYY HH24:MI:SS')
                              ilike concat('%', :valorFiltro, '%')
                          -- 🔹 fechaSalida
                          or to_char(shfi.fecha_fin, 'DD/MM/YYYY HH24:MI:SS')
                              ilike concat('%', :valorFiltro, '%'))
                      )
                  order by nombreCompleto, fechaIngreso desc
                """,
            countQuery = """
                  select count(*) from (
                   select
                     distinct
                     upper(trim(concat_ws(' ',
                       trim(ifi.nombres),
                       trim(ifi.apellido_paterno),
                       trim(ifi.apellido_materno)
                     ))) as nombreCompleto,
                     ifi.numero_identificacion as numeroIdentificacion,
                     sj.nombre as centro,
                     coalesce(
                       iem.num_expediente,
                       (
                         select iem2.num_expediente
                         from ia_expediente_matriz iem2
                         where iem2.id_ficha_identificacion = ifi.id_ficha_identificacion
                         and iem2.fecha_creacion between shfi.fecha_inicio  and shfi.fecha_fin
                         order by iem2.fecha_creacion desc
                         limit 1
                       )
                     ) as numeroExpediente,
                     TO_CHAR(shfi.fecha_inicio, 'DD/MM/YYYY HH24:MI:SS') as fechaIngreso,
                     TO_CHAR(shfi.fecha_fin,   'DD/MM/YYYY HH24:MI:SS') as fechaSalida,
                     --shfi.fecha_inicio as fechaIngreso,
                     --shfi.fecha_fin as fechaSalida,
                     pc2.nombre as motivoIngreso,
                     pc.nombre as motivoSalida,
                     shfi.observacion_ingreso as observacionIngreso,
                     sr.observaciones as observacionSalida
                   from seg_historico_ficha_identificacion shfi
                   join ia_ficha_identificacion ifi on ifi.id_ficha_identificacion = shfi.id_ficha_identificacion
                   join salida_registro sr on sr.id_registro_salida = shfi.id_registro_salida
                   left join ia_acta_externamiento iae on iae.id_acta_externamiento = sr.id_externamiento
                   left join ia_expediente_matriz iem on iem.id_expediente = iae.id_expediente_matriz
                   left join par_catalogo pc on pc.id_catalogo = sr.id_catalogo_motivo_salida
                   left join par_catalogo pc2 on pc2.id_catalogo = shfi.id_catalogo_tipo_ingreso
                   left join seg_jerarquia sj on sj.id_jerarquia = shfi.id_centro
                   where ((:valorCentro is null
                          or :valorCentro = ''
                          or sj.nombre ilike concat('%', :valorCentro, '%'))
                          and
                          (:valorFiltro is null
                          or :valorFiltro = ''
                          or upper(trim(concat_ws(' ',
                                trim(ifi.nombres),
                                trim(ifi.apellido_paterno),
                                trim(ifi.apellido_materno)
                              ))) ilike concat('%', :valorFiltro, '%')
                          or ifi.numero_identificacion ilike concat('%', :valorFiltro, '%')
                          or pc2.nombre ilike concat('%', :valorFiltro, '%')
                          or pc.nombre ilike concat('%', :valorFiltro, '%')
                          or shfi.observacion_ingreso ilike concat('%', :valorFiltro, '%')
                          or sr.observaciones ilike concat('%', :valorFiltro, '%')
                          -- 🔹 numeroExpediente
                                  or coalesce(
                              iem.num_expediente,
                              (
                                  select iem2.num_expediente
                                  from ia_expediente_matriz iem2
                                  where iem2.id_ficha_identificacion = ifi.id_ficha_identificacion
                                  and iem2.fecha_creacion between shfi.fecha_inicio and shfi.fecha_fin
                                  order by iem2.fecha_creacion desc
                                  limit 1
                              )
                          ) ilike concat('%', :valorFiltro, '%')
                          -- 🔹 fechaIngreso
                          or to_char(shfi.fecha_inicio, 'DD/MM/YYYY HH24:MI:SS')
                              ilike concat('%', :valorFiltro, '%')
                          -- 🔹 fechaSalida
                          or to_char(shfi.fecha_fin, 'DD/MM/YYYY HH24:MI:SS')
                              ilike concat('%', :valorFiltro, '%'))
                      )
                 ) t
                """,
            nativeQuery = true
    )
    Page<AdolescenteExternadoDTO> obtenerDatosReporte(
            @Param("valorFiltro") String valorFiltro,
            @Param("valorCentro") String valorCentro,
            Pageable pageable);
}
