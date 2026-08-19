package net.latinus.sistema.integral.gestion.seguridad.model.both.institucion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CamposDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idAdolescenteSeguimiento"}, callSuper = true)
public class SeguimientoAdolescInstDTO extends CamposDTO{
    private Long idAdolescenteSeguimiento;
    private String medioEntrevista;
    private String resultadoEntrevista;
    private String recomendacion;
    private String observacion;
    private Date fechaSeguimiento;
    private AdolescenteDerivadoInstDTO adolescenteDerivadoInst;
}
