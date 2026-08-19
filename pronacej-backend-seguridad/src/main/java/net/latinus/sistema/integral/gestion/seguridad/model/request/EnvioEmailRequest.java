package net.latinus.sistema.integral.gestion.seguridad.model.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.List;

@Data
public class EnvioEmailRequest {

    private List<String> emailsTo;
    private String razon;
    private String contenido;
    private String tokenEmpresa;
    private String tipo;
    private MultipartFile[] multipartFiles;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
