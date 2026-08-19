package net.latinus.sistema.integral.gestion.seguridad.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class LocalDataAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    @Override
    public JsonElement serialize(LocalDate date, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(date.toString());
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
        return LocalDate.parse(json.getAsString());
    }
}