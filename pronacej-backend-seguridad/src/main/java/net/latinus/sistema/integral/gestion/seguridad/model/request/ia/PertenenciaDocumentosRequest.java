package net.latinus.sistema.integral.gestion.seguridad.model.request.ia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;

@Data
@EqualsAndHashCode(of = {"tokenIdentificadorPertenencia"}, callSuper = true)
public class PertenenciaDocumentosRequest extends PaginacionRequest {
    private String tokenIdentificadorPertenencia;
    private String textoBuscar;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
