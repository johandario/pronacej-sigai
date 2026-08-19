package net.latinus.sistema.integral.gestion.seguridad.model.request;

import lombok.Data;

import java.util.List;

@Data
public class ExportacionRequest {
    private List<String> numerosIdentificacion;
    private List<String> nemonicosSecciones;
}

