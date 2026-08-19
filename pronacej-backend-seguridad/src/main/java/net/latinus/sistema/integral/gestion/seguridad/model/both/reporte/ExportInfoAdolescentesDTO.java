package net.latinus.sistema.integral.gestion.seguridad.model.both.reporte;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExportInfoAdolescentesDTO {

    @CsvBindByName(column = "id")
    private Long id;

    @CsvBindByName(column = "nombre")
    private String nombre;

    @CsvBindByName(column = "numero_expediente")
    private String numeroExpediente;

    @CsvBindByName(column = "descripcion")
    private String descripcion;

    @Override
    public String toString() {
        return FuncionesAyuda.toStringHelp(this);
    }
}

