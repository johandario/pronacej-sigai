package net.latinus.sistema.integral.gestion.seguridad.model.both;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

@Data
@EqualsAndHashCode(callSuper = true)
public class FichaIngresoDTO extends CamposDTO implements Serializable {

    private String tokenIdentificadorFichaIdentificacion;
    private Date fechaIngreso;
    private String tokenIdentificadorCentro;
    private JerarquiaDTO centro;
    private Boolean atencionSalud;
    private String motivo;
    private String observaciones;
    private String responsableInscripcion;
    private String caracteristicasParticulares;
    private String tokenIdentificadorProgramaDerivado; // Catalogo
    private String tokenIdentificadorTutor; // Catalogo por ahora
    private Boolean lesiones;
    private String especificarZonaLesiones;
    private Boolean moretones;
    private String especificarZonaMoretones;
    private Boolean cicatrices;
    private String especificarZonaCicatrices;
    private Boolean tatuajes;
    private String especificarZonaTatuajes;
    private Boolean piercing;
    private String especificarZonaPiercing;
    private Boolean otros;
    private String especificarZonaOtros;
    private Boolean victimaAgresion;
    private String especificarAgresion;
    private String tokenIdentificadorSeguroSalud; // Catalogo
    private String tokenIdentificadorFormaCabeza; // Catalogo
    private String tokenIdentificadorFormaNariz; // Catalogo
    private String tokenIdentificadorFormaLabios; // Catalogo
    private String tokenIdentificadorFormaCuerpo; // Catalogo
    private String tokenIdentificadorAnomaliaOjos; // Catalogo
    private Boolean esEmbarazada;
    private Integer mesesEmbarazo;
    private Boolean ingresaConHijo;

    private String tokenIdentificadorCarpeta;
    
    private DatosHijoIngresadoDTO datosHijoIngresado;

    private String nombreSeguro;

    private CatalogoDTO estadoAdolescente;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
