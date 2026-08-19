package net.latinus.sistema.integral.gestion.seguridad.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para configuración centralizada de GSON con adaptadores para tipos java.time.*
 * Resuelve problemas de reflexión en Java 9+ con GSON y tipos de fecha modernos
 * 
 * @author welli
 */
public class GsonConfigUtil {
    
    /**
     * Crea una instancia de GSON configurada con adaptadores para tipos java.time.*
     * Esto resuelve problemas de reflexión en Java 9+ con GSON
     * 
     * @return Gson configurado con adaptadores para LocalDate y LocalDateTime
     */
    public static Gson createConfiguredGson() {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) -> 
                LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (localDate, type, context) -> 
                context.serialize(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, context) -> 
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (localDateTime, type, context) -> 
                context.serialize(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .setDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER)
            .create();
    }
}