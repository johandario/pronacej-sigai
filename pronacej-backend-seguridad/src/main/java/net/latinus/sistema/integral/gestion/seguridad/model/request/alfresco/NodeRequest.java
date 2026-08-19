package net.latinus.sistema.integral.gestion.seguridad.model.request.alfresco;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import net.latinus.sistema.integral.gestion.seguridad.model.request.Serializable;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.utils.FuncionesAyuda;

import java.text.SimpleDateFormat;
import java.util.Map;

@Data
public class NodeRequest implements Serializable {

    private String name;
    private String nodeType;
    private Map<String, String> properties;

    //Example
    /*
    {
      "name": "My Folder",
      "nodeType": "cm:folder",
      "properties": {
        "cm:title": "My Folder",
        "cm:description": "My new folder"
      }
    }
     */

    @Override
    public String toString() {
                return FuncionesAyuda.toStringHelp(this);
    }
}
