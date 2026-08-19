package net.latinus.sistema.integral.gestion.seguridad.model.both.reporte;

import lombok.*;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdolescenteExternadoDTO {
    private String nombreCompleto;
    private String numeroIdentificacion;
    private String centro;
    private String numeroExpediente;
    private String fechaIngreso;
    private String fechaSalida;
    private String motivoIngreso;
    private String motivoSalida;
    private String observacionIngreso;
    private String observacionSalida;
}
