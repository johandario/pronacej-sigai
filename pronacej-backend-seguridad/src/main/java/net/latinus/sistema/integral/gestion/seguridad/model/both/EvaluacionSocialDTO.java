package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class EvaluacionSocialDTO extends CamposDTO implements Serializable {
    
    private String tokenIdentificadorFichaIdentificacion;
    private String tokenIdentificadorZonaVivienda;
    private String tokenIdentificadorSubZona;
    private String tokenIdentificadorMaterialParedVivienda;
    private String tokenIdentificadorMaterialPisoVivienda;
    private String tokenIdentificadorMaterialTechoVivienda;
    private String tokenIdentificadorAbastecimientoAguaVivienda;
    private String tokenIdentificadorTipoVivienda;
    private String tokenIdentificadorTipoAlumbradoVivienda;
    private String tokenIdentificadorCombustibleCocinarVivienda;
    private String tokenIdentificadorTipoDesagueVivienda;
    private String tokenIdentificadorTenencia;
    private String tokenIdentificadorOtrosServicios;
    private Integer numeroAmbientes;
    private Integer numeroOcupantes;
    private Integer numeroHabitaciones;
    private Integer numeroDormitorios;
    private String grupoAmical;
    private String factorRiesgoMedio;
    private String areaAcademicoLaboral;
    private String areaSocialRecreacional;
    private String areaFamiliarPareja;
    private String areaPersonal;
    
    private String nombreCompletoUsuarioCreacion;
    
    private List<PersonaRelacionadaDTO> listaPersonasRelacionadas;
    private List<EvaluacionSocialArtefactoDTO> listaArtefactos;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
    
}
