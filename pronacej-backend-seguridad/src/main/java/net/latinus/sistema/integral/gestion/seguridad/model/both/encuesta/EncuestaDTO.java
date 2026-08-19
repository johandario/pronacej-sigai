package net.latinus.sistema.integral.gestion.seguridad.model.both.encuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idEncuesta"}, callSuper = true)
public class EncuestaDTO extends CamposDTO implements Serializable {
    private long idEncuesta;
    private String nombre;
    private String descripcion;
    private Boolean seccionesOrdenadas;
    private Long idJerarquia;
    private String nemonico;
    private String nemonicoCentro;
    private String tipoCentro;
    private String nemonicoCategoria;
    private String categoria;
    private String adolescente;
    private String dniAdolescente;
    private List<SeccionDTO> secciones = new ArrayList<>();
}
