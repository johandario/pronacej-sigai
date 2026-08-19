package net.latinus.sistema.integral.gestion.seguridad.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.both.CatalogoDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class TipoDeArchivoSeccionFichaPrincipal {

    private CatalogoDTO seccionFichaPPL;
    private Long cantidadDeTipoDedocumentos;

    private Date fechaDeCreacion;

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
