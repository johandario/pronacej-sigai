package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApreciacionFinalTratamientoDTO extends CamposDTO implements Serializable {
    
    // Identificador de la ficha de identificación
    private String tokenIdentificadorFichaIdentificacion;
    
    // Nombre completo del usuario que creó el registro
    private String nombreCompletoUsuarioCreacion;
    
    // Listas que contienen los datos reales
    private List<SituacionActualAdolescenteDTO> listaSituaciones;
    private List<FactoresPresentesDTO> listaFactoresPresentes;
    
    // Fecha de registro mostrada en la interfaz
    private Date fechaRegistro;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}