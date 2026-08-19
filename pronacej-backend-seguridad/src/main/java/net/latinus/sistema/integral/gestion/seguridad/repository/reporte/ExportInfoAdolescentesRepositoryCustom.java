package net.latinus.sistema.integral.gestion.seguridad.repository.reporte;

import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionDinamicaResultado;

import java.util.List;

public interface ExportInfoAdolescentesRepositoryCustom {

    ExportacionDinamicaResultado obtenerAdolescentesParaExportar(
            List<String> numerosIdentificacion,
            List<String> nemonicosSecciones
    );
}

