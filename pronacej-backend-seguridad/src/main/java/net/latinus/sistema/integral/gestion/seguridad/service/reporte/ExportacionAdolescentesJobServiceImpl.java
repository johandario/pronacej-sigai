package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import jakarta.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.EstadoExportacionJob;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionEstadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionJob;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.PlantillaExportacion;
import net.latinus.sistema.integral.gestion.seguridad.repository.reporte.ExportInfoAdolescentesRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.service.reporte.ExportacionAdolescentesJobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ExportacionAdolescentesJobServiceImpl
implements ExportacionAdolescentesJobService {
    private final LogService log = new LogService(ExportacionAdolescentesJobServiceImpl.class);
    private final ExportInfoAdolescentesRepository exportInfoAdolescentesRepository;
    private final int tamanoLote;
    private final Path directorioTemporal;
    private final Map<String, ExportacionJob> jobs = new ConcurrentHashMap<String, ExportacionJob>();
    private final Map<String, String> tokensAJob = new ConcurrentHashMap<String, String>();
    private final ExecutorService executor;

    public ExportacionAdolescentesJobServiceImpl(ExportInfoAdolescentesRepository exportInfoAdolescentesRepository, @Value(value="${pronacej.exportacion.tamano-lote:25}") int tamanoLote, @Value(value="${pronacej.exportacion.directorio-temporal:${java.io.tmpdir}/pronacej-exportaciones}") String directorioTemporal) {
        this.exportInfoAdolescentesRepository = exportInfoAdolescentesRepository;
        this.tamanoLote = tamanoLote;
        this.directorioTemporal = Paths.get(directorioTemporal, new String[0]);
        AtomicInteger contadorHilos = new AtomicInteger(1);
        ThreadFactory threadFactory = runnable -> {
            Thread hilo = new Thread(runnable, "exportacion-adolescentes-" + contadorHilos.getAndIncrement());
            hilo.setDaemon(true);
            return hilo;
        };
        this.executor = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), threadFactory);
    }

    @PreDestroy
    void apagar() {
        this.executor.shutdown();
    }

    @Override
    public synchronized String iniciarJob(List<String> numerosIdentificacion, List<String> nemonicosSecciones, Long idUsuarioSistema) {
        if (!this.jobs.isEmpty()) {
            throw new IllegalStateException("Ya hay una exportaci\u00f3n en curso en el sistema. Espere a que finalice o desc\u00e1rtela antes de iniciar una nueva.");
        }
        String jobId = UUID.randomUUID().toString();
        ExportacionJob job = new ExportacionJob(jobId, idUsuarioSistema, numerosIdentificacion.size(), nemonicosSecciones.size());
        this.jobs.put(jobId, job);
        this.executor.submit(() -> this.procesarJob(job, numerosIdentificacion, nemonicosSecciones));
        return jobId;
    }

    @Override
    public ExportacionEstadoDTO consultarEstado(String jobId, Long idUsuarioSistema) {
        ExportacionJob job = this.jobs.get(jobId);
        if (job == null || !job.getIdUsuarioSistema().equals(idUsuarioSistema)) {
            throw new NoSuchElementException("No se encontr\u00f3 el proceso de exportaci\u00f3n solicitado.");
        }
        return this.construirEstadoDTO(job, idUsuarioSistema);
    }

    @Override
    public List<ExportacionEstadoDTO> listarJobs(Long idUsuarioSistema) {
        ArrayList<ExportacionEstadoDTO> resultado = new ArrayList<ExportacionEstadoDTO>();
        for (ExportacionJob job : this.jobs.values()) {
            resultado.add(this.construirEstadoDTO(job, idUsuarioSistema));
        }
        return resultado;
    }

    @Override
    public void cancelarJob(String jobId, Long idUsuarioSistema) {
        ExportacionJob job = this.jobs.get(jobId);
        if (job == null || !job.getIdUsuarioSistema().equals(idUsuarioSistema)) {
            throw new NoSuchElementException("No se encontr\u00f3 el proceso de exportaci\u00f3n solicitado.");
        }
        if (job.getEstado() != EstadoExportacionJob.PENDIENTE && job.getEstado() != EstadoExportacionJob.EN_PROGRESO) {
            throw new IllegalStateException("El proceso ya finaliz\u00f3 y no puede cancelarse.");
        }
        job.setCancelacionSolicitada(true);
    }

    @Override
    public void descartarJob(String jobId, Long idUsuarioSistema) {
        boolean esTerminal;
        ExportacionJob job = this.jobs.get(jobId);
        if (job == null || !job.getIdUsuarioSistema().equals(idUsuarioSistema)) {
            throw new NoSuchElementException("No se encontr\u00f3 el proceso de exportaci\u00f3n solicitado.");
        }
        boolean bl = esTerminal = job.getEstado() == EstadoExportacionJob.COMPLETADO || job.getEstado() == EstadoExportacionJob.ERROR || job.getEstado() == EstadoExportacionJob.CANCELADO;
        if (!esTerminal) {
            throw new IllegalStateException("El proceso todav\u00eda est\u00e1 en curso y no puede descartarse.");
        }
        this.jobs.remove(jobId);
        if (job.getTokenDescarga() != null) {
            this.tokensAJob.remove(job.getTokenDescarga());
        }
        this.borrarArchivoSilencioso(job.getRutaArchivo());
    }

    private ExportacionEstadoDTO construirEstadoDTO(ExportacionJob job, Long idUsuarioSistema) {
        ExportacionEstadoDTO dto = new ExportacionEstadoDTO();
        dto.setJobId(job.getId());
        dto.setEstado(job.getEstado().name());
        dto.setLoteActual(job.getLoteActual());
        dto.setTotalLotes(job.getTotalLotes());
        dto.setRegistrosProcesados(job.getRegistrosProcesados());
        dto.setEsPropio(job.getIdUsuarioSistema().equals(idUsuarioSistema));
        dto.setTotalAdolescentesSolicitados(job.getTotalAdolescentesSolicitados());
        dto.setTotalSeccionesSolicitadas(job.getTotalSeccionesSolicitadas());
        if (job.getEstado() == EstadoExportacionJob.COMPLETADO && !job.isTokenConsumido()) {
            dto.setTokenDescarga(job.getTokenDescarga());
        }
        if (job.getEstado() == EstadoExportacionJob.ERROR) {
            dto.setMensajeError(job.getMensajeError());
        }
        if (job.getEstado() == EstadoExportacionJob.EN_PROGRESO || job.getEstado() == EstadoExportacionJob.COMPLETADO) {
            dto.setTamanoBytes(this.obtenerTamanoArchivo(job.getRutaArchivo()));
            dto.setTamanoOriginalBytes(job.getTamanoOriginalBytes());
        }
        return dto;
    }

    private Long obtenerTamanoArchivo(Path archivo) {
        if (archivo == null) {
            return null;
        }
        try {
            return Files.size(archivo);
        }
        catch (IOException e) {
            return null;
        }
    }

    @Override
    public Path resolverArchivoParaDescarga(String token) {
        ExportacionJob job;
        String jobId = this.tokensAJob.get(token);
        ExportacionJob exportacionJob = job = jobId != null ? this.jobs.get(jobId) : null;
        if (job == null || job.getEstado() != EstadoExportacionJob.COMPLETADO || job.isTokenConsumido()) {
            throw new NoSuchElementException("El enlace de descarga es inv\u00e1lido o ya fue utilizado.");
        }
        job.setTokenConsumido(true);
        return job.getRutaArchivo();
    }

    @Override
    public void finalizarDescarga(String token) {
        String jobId = this.tokensAJob.remove(token);
        if (jobId == null) {
            return;
        }
        ExportacionJob job = this.jobs.remove(jobId);
        this.borrarArchivoSilencioso(job != null ? job.getRutaArchivo() : null);
    }

    @Scheduled(fixedDelay=1800000L)
    void limpiarJobsAbandonados() {
        Instant ahora = Instant.now();
        Iterator<Map.Entry<String, ExportacionJob>> it = this.jobs.entrySet().iterator();
        while (it.hasNext()) {
            ExportacionJob job = it.next().getValue();
            boolean colgado = Duration.between(job.getFechaCreacion(), ahora).toHours() >= 6L;
            if (!colgado) continue;
            it.remove();
            if (job.getTokenDescarga() != null) {
                this.tokensAJob.remove(job.getTokenDescarga());
            }
            this.borrarArchivoSilencioso(job.getRutaArchivo());
        }
    }

    private void procesarJob(ExportacionJob job, List<String> numerosIdentificacion, List<String> nemonicosSecciones) {
        job.setEstado(EstadoExportacionJob.EN_PROGRESO);
        Path archivo = null;
        try {
            Files.createDirectories(this.directorioTemporal, new FileAttribute[0]);
            archivo = this.directorioTemporal.resolve("export-" + job.getId() + ".zip");
            job.setRutaArchivo(archivo);
            PlantillaExportacion plantilla = this.exportInfoAdolescentesRepository.construirPlantilla(nemonicosSecciones);
            int tamanoLoteEfectivo = this.calcularTamanoLoteEfectivo(nemonicosSecciones);
            List<List<String>> lotes = this.particionar(numerosIdentificacion, tamanoLoteEfectivo);
            job.setTotalLotes(Math.max(lotes.size(), 1));
            this.log.info("Job de exportaci\u00f3n '" + job.getId() + "': tama\u00f1o de lote efectivo=" + tamanoLoteEfectivo + " (base=" + this.tamanoLote + "), lotes=" + lotes.size());
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archivo.toFile()));){
                zip.setLevel(9);
                zip.putNextEntry(new ZipEntry("informacion_adolescentes.csv"));
                ConteoBytesOutputStream conteo = new ConteoBytesOutputStream(zip);
                OutputStreamWriter writer = new OutputStreamWriter((OutputStream)conteo, StandardCharsets.UTF_8);
                ((Writer)writer).write(65279);
                this.escribirLineaCsv(writer, plantilla.getHeaders());
                int registrosProcesados = 0;
                int loteActual = 0;
                for (List<String> lote : lotes) {
                    this.log.info("Job '" + job.getId() + "': consultando lote " + (loteActual + 1) + "/" + lotes.size() + " (" + lote.size() + " adolescentes)");
                    long inicioLote = System.currentTimeMillis();
                    int filasLote = this.exportInfoAdolescentesRepository.obtenerFilasLote(plantilla, lote, fila -> {
                        if (job.isCancelacionSolicitada()) {
                            throw new JobCanceladoException();
                        }
                        ArrayList<String> valores = new ArrayList<String>(fila.size());
                        for (Object valor : fila) {
                            valores.add(valor == null ? "" : valor.toString());
                        }
                        try {
                            this.escribirLineaCsv(writer, valores);
                        }
                        catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                    this.log.info("Job '" + job.getId() + "': lote " + (loteActual + 1) + "/" + lotes.size() + " resuelto en " + (System.currentTimeMillis() - inicioLote) + " ms, filas=" + filasLote);
                    ((Writer)writer).flush();
                    job.setLoteActual(++loteActual);
                    job.setRegistrosProcesados(registrosProcesados += filasLote);
                    job.setTamanoOriginalBytes(conteo.getConteo());
                    if (!job.isCancelacionSolicitada()) continue;
                    throw new JobCanceladoException();
                }
                ((Writer)writer).flush();
                zip.closeEntry();
            }
            String token = UUID.randomUUID().toString();
            job.setTokenDescarga(token);
            this.tokensAJob.put(token, job.getId());
            job.setEstado(EstadoExportacionJob.COMPLETADO);
        }
        catch (JobCanceladoException e) {
            this.log.info("Job de exportaci\u00f3n '" + job.getId() + "' cancelado por el usuario.");
            job.setEstado(EstadoExportacionJob.CANCELADO);
            this.borrarArchivoSilencioso(archivo);
        }
        catch (Throwable e) {
            this.log.error("Error procesando job de exportaci\u00f3n de adolescentes '" + job.getId() + "': " + e.getClass().getName() + ": " + e.getMessage());
            job.setEstado(EstadoExportacionJob.ERROR);
            job.setMensajeError("No fue posible generar la exportaci\u00f3n. Consulte con su administrador.");
            this.borrarArchivoSilencioso(archivo);
        }
    }

    private int calcularTamanoLoteEfectivo(List<String> nemonicosSecciones) {
        long numeroSeccionesDinamicas = this.exportInfoAdolescentesRepository.contarSeccionesDinamicas(nemonicosSecciones);
        if (numeroSeccionesDinamicas <= 0L) {
            return this.tamanoLote;
        }
        return (int)Math.max(1L, (long)this.tamanoLote / (numeroSeccionesDinamicas + 1L));
    }

    private List<List<String>> particionar(List<String> valores, int tamano) {
        ArrayList<List<String>> lotes = new ArrayList<List<String>>();
        for (int i = 0; i < valores.size(); i += tamano) {
            lotes.add(valores.subList(i, Math.min(i + tamano, valores.size())));
        }
        return lotes;
    }

    private void escribirLineaCsv(Writer writer, List<String> valores) throws IOException {
        for (int i = 0; i < valores.size(); ++i) {
            if (i > 0) {
                writer.write(44);
            }
            writer.write(this.escapeCsv(valores.get(i)));
        }
        writer.write(10);
    }

    private String escapeCsv(String valor) {
        if (valor == null) {
            return "";
        }
        boolean requiereComillas = valor.contains(",") || valor.contains("\n") || valor.contains("\r") || valor.contains("\"");
        String escapado = valor.replace("\"", "\"\"");
        return requiereComillas ? "\"" + escapado + "\"" : escapado;
    }

    private void borrarArchivoSilencioso(Path archivo) {
        if (archivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(archivo);
        }
        catch (Exception e) {
            this.log.warn("No fue posible eliminar el archivo temporal de exportaci\u00f3n '" + String.valueOf(archivo) + "': " + e.getMessage());
        }
    }

    private static final class ConteoBytesOutputStream
    extends FilterOutputStream {
        private long conteo = 0L;

        ConteoBytesOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            this.out.write(b);
            ++this.conteo;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.out.write(b, off, len);
            this.conteo += (long)len;
        }

        long getConteo() {
            return this.conteo;
        }
    }

    private static final class JobCanceladoException
    extends RuntimeException {
        JobCanceladoException() {
            super(null, null, false, false);
        }
    }
}
