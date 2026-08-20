package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import java.nio.file.Path;
import java.util.List;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionEstadoDTO;

public interface ExportacionAdolescentesJobService {
    public String iniciarJob(List<String> var1, List<String> var2, Long var3);

    public ExportacionEstadoDTO consultarEstado(String var1, Long var2);

    public List<ExportacionEstadoDTO> listarJobs(Long var1);

    public void cancelarJob(String var1, Long var2);

    public void descartarJob(String var1, Long var2);

    public Path resolverArchivoParaDescarga(String var1);

    public void finalizarDescarga(String var1);
}
