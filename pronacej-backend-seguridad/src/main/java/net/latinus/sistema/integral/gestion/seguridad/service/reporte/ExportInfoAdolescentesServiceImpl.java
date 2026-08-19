package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.ExportacionDinamicaResultado;
import net.latinus.sistema.integral.gestion.seguridad.model.request.ExportacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.reporte.ExportInfoAdolescentesRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportInfoAdolescentesServiceImpl implements ExportInfoAdolescentesService {

    private final ExportInfoAdolescentesRepository exportInfoAdolescentesRepository;
    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<byte[]> exportarAdolescentes(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<byte[]> df = new RespuestaPorDefectoAuditoria<>();

        try {
            // Validar JWT
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            // Desencriptar body
            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            ExportacionRequest exportacionRequest = new Gson().fromJson(df22.getData(), ExportacionRequest.class);

            List<String> numerosIdentificacion = normalizarLista(exportacionRequest != null
                    ? exportacionRequest.getNumerosIdentificacion()
                    : null);

            if (numerosIdentificacion.isEmpty()) {
                df.setMensaje("Debe enviar al menos un numero de identificacion para exportar.");
                return df;
            }

            List<String> nemonicosSecciones = normalizarLista(exportacionRequest != null
                    ? exportacionRequest.getNemonicosSecciones()
                    : null);

            ExportacionDinamicaResultado resultado = this.exportInfoAdolescentesRepository
                    .obtenerAdolescentesParaExportar(numerosIdentificacion, nemonicosSecciones);

            // Generar CSV
            byte[] csvBytes = this.generarCsv(resultado);

            // Preparar respuesta exitosa
            int totalFilas = resultado != null && resultado.getFilas() != null
                    ? resultado.getFilas().size()
                    : 0;
            String mensajeUsuario = "Se exportaron exitosamente " + totalFilas + " registros de adolescentes";
            String mensajeAuditoria = "Exportación de " + totalFilas + " registros de adolescentes realizada";

            df.llenarRespuestaExitosa(mensajeUsuario, csvBytes, mensajeAuditoria);

        } catch (Exception e) {
            df.llenarConDatosDeException(e);
        }

        return df;
    }

    private List<String> normalizarLista(List<String> listaEntrada) {
        List<String> salida = new ArrayList<>();
        if (listaEntrada == null) {
            return salida;
        }

        for (String valor : listaEntrada) {
            if (valor == null) {
                continue;
            }
            String limpio = valor.trim();
            if (!limpio.isEmpty()) {
                salida.add(limpio);
            }
        }

        return salida;
    }

    private byte[] generarCsv(ExportacionDinamicaResultado resultado) {
        StringBuilder csv = new StringBuilder();

        List<String> headers = resultado != null && resultado.getHeaders() != null
                ? resultado.getHeaders()
                : new ArrayList<>();
        List<List<Object>> filas = resultado != null && resultado.getFilas() != null
                ? resultado.getFilas()
                : new ArrayList<>();

        if (!headers.isEmpty()) {
            appendCsvLine(csv, headers);
        }

        for (List<Object> fila : filas) {
            List<String> valores = new ArrayList<>();
            if (fila != null) {
                for (Object valor : fila) {
                    valores.add(valor == null ? "" : valor.toString());
                }
            }
            appendCsvLine(csv, valores);
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendCsvLine(StringBuilder csv, List<String> valores) {
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escapeCsv(valores.get(i)));
        }
        csv.append('\n');
    }

    private String escapeCsv(String valor) {
        if (valor == null) {
            return "";
        }
        boolean requiereComillas = valor.contains(",") || valor.contains("\n") || valor.contains("\r") || valor.contains("\"");
        String escapado = valor.replace("\"", "\"\"");
        return requiereComillas ? "\"" + escapado + "\"" : escapado;
    }
}

