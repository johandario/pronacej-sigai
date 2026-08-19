package net.latinus.sistema.integral.gestion.seguridad.model.both.ia.ficha_medica;

import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;

import java.util.Date;

@Data
public class IngresoCentroJuvenilDTO {
    private String tokenIdentificador;

    private String tokenIdFichaIdentificacion;

    private String centro;

    private Date fechaIngreso;

    private Date fechaEgreso;

    private CatalogoDTO motivo;
}
