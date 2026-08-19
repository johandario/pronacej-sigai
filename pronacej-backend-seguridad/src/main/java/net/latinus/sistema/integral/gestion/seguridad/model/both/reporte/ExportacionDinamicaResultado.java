package net.latinus.sistema.integral.gestion.seguridad.model.both.reporte;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportacionDinamicaResultado {
    private List<String> headers;
    private List<List<Object>> filas;
}

