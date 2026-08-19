package net.latinus.sistema.integral.gestion.seguridad.model.both.informe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idInforme"}, callSuper = true)
public class InformeDTO extends CamposDTO {
    private long idInforme;
    private Date fechaRegistro;
    private String asignado;
    private String tipo;
    private Boolean impreso;
    private Boolean firmado;
    private Long idFichaIdentificacion;
    private String tokenFichaIdentificacion;
    private Long idPlantillaInforme;
    private String nemonicoPlantillaInforme;
    private Long idInformePadre;
    private List<ValorInformeDTO> valores;
    private InformeDocumentoDTO informeDocumentoDTO;
    private String nombreAdolescente;

    public String getNombreAdolescente() {
        return nombreAdolescente;
    }

    public void setNombreAdolescente(String nombreAdolescente) {
        this.nombreAdolescente = nombreAdolescente;
    }

}
