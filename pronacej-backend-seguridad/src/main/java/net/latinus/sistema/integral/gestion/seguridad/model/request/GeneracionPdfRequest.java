package net.latinus.sistema.integral.gestion.seguridad.model.request;

import lombok.Data;

import java.util.Map;

@Data
public class GeneracionPdfRequest {
    private String nemonico;
    private Map<String, String> variables;
}
