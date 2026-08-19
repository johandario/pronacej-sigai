package net.latinus.sistema.integral.gestion.seguridad.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class FuncionesAyuda {

    /**
     * Crea una cadena con caracteres aleatorios definidos
     *
     * @param longitud int longuitud de la cadena a crear.
     * @return String
     */
    public String crearCadenaAleatoria(Integer longitud) {

        String banco = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        // La cadena en donde iremos agregando un carácter aleatorio
        String cadena = "";
        for (int x = 0; x < longitud; x++) {
            int indiceAleatorio = numeroAleatorioEnRango(0, banco.length() - 1);
            char caracterAleatorio = banco.charAt(indiceAleatorio);
            cadena += caracterAleatorio;
        }
        return cadena;
    }

    public int numeroAleatorioEnRango(int minimo, int maximo) {
        // nextInt regresa en rango pero con límite superior exclusivo, por eso sumamos 1
        return ThreadLocalRandom.current().nextInt(minimo, maximo + 1);
    }

    public String reemplarTildesPorCaracterEspecial(String cadena) {
        if (cadena == null || cadena.isEmpty()) {
            return cadena;
        }
        cadena = cadena.replace("á", "&aacute;");
        cadena = cadena.replace("é", "&eacute;");
        cadena = cadena.replace("í", "&iacute;");
        cadena = cadena.replace("ó", "&oacute;");
        cadena = cadena.replace("ú", "&uacute;");
        cadena = cadena.replace("ñ", "&ntilde;");

        cadena = cadena.replace("Ñ", "&Ntilde;");
        cadena = cadena.replace("Á", "&Aacute;");
        cadena = cadena.replace("É", "&Eacute;");
        cadena = cadena.replace("Í", "&Iacute;");
        cadena = cadena.replace("Ó", "&Oacute;");
        cadena = cadena.replace("Ú", "&Uacute;");
        return cadena;
    }

    /**
     * Crea una cadena con caracteres cubiertos con X
     *
     * @param palabra  String palabra que se va a cubrir con X.
     * @param estado   String inicio cubre al inicio de la palabra, fin cubre al fin de la palabra, otros cubre en medio.
     * @param cantidad int cantidad de X que se usaran.
     * @return String
     */
    public String cubrirPalabra(String palabra, String estado, int cantidad) {
        //estado inicio , fin , cualquier cosa(este caso toma en medio);
        String caracterOcultar = "X";
        if (palabra.length() < cantidad) {
            return palabra;
        } else {
            String subString;
            if (estado.equals("inicio")) {
                subString = palabra.substring(0, cantidad);
            } else if (estado.equals("fin")) {
                subString = palabra.substring(palabra.length() - cantidad, cantidad);
            } else {
                //En la mitad
                int indexMitad = (palabra.length() / 2) - cantidad;
                subString = palabra.substring(indexMitad, indexMitad + cantidad);
            }

            palabra = palabra.replace(subString, new String(new char[cantidad]).replace("\0", caracterOcultar));
            return palabra;

        }
    }

    /**
     * Crea un json text con un objeto de clase general
     *
     * @param objecto Object
     * @return String
     */

    public static String toStringHelp(Object objecto) {
        try {
            Gson gson = new GsonBuilder().setDateFormat(EtiquetaNemonico.FORMAT_DATE_GSON_BUILDER).create();
            return gson.toJson(objecto);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger logger = LoggerFactory.getLogger(e.getClass());
            logger.error(e.toString());
            return "";
        }
    }

    /**
     * Descomprimir un objeto serializado
     *
     * @param objeto Object
     * @return String
     */

    public static String descomprimirBase64Gzip(String objeto) {
        try {
            byte[] compressedBytes = Base64.getDecoder().decode(objeto);
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressedBytes));
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger logger = LoggerFactory.getLogger(e.getClass());
            logger.error(e.toString());
            return "";
        }
    }

    public static String fechaATexto(Date fechaEntrante, Boolean mostrarHoraMinutos, Boolean mostrarSegundos) {
        if (fechaEntrante == null) return "";

        StringBuilder pattern = new StringBuilder("dd/MM/yyyy");

        if (Boolean.TRUE.equals(mostrarHoraMinutos) || Boolean.TRUE.equals(mostrarSegundos)) {
            pattern.append(" HH:mm");
            if (Boolean.TRUE.equals(mostrarSegundos)) {
                pattern.append(":ss");
            }
        }

        DateFormat fecha = new SimpleDateFormat(pattern.toString());
        return fecha.format(fechaEntrante);
    }

    public static String fechaATexto(Date fechaEntrante, Boolean mostrarHoraMinutos, Boolean mostrarSegundos, String formatoFecha) {
        if (fechaEntrante == null) return "";

        if (formatoFecha == null || formatoFecha.isBlank()) formatoFecha = "dd/MM/yyyy";

        StringBuilder pattern = new StringBuilder(formatoFecha);

        if (Boolean.TRUE.equals(mostrarHoraMinutos) || Boolean.TRUE.equals(mostrarSegundos)) {
            pattern.append(" HH:mm");
            if (Boolean.TRUE.equals(mostrarSegundos)) {
                pattern.append(":ss");
            }
        }

        DateFormat fecha = new SimpleDateFormat(pattern.toString());
        return fecha.format(fechaEntrante);
    }

    public static LocalDateTime toLocalDateTime(Date fecha) {
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Crea un nemonico con prefijo a partir de un texto
     * @param prefijo prefijo que se va a agregar al nemonico
     * @param texto texto que se va a usar para crear el nemonico
     * @return cadena con el prefijo y nemonico
     */
    public static String crearNemonico(String prefijo, String texto) {

        if (prefijo == null) prefijo = "";
        else prefijo = prefijo.toUpperCase()  + "_";

        if (texto == null) texto = "";

        // 1. Reemplazar guiones medios por guiones bajos (ANTES de normalizar)
        String resultado = texto.replace("–", "_");

        // 2. Normalizar para eliminar acentos y caracteres especiales como ñ → n
        String normalizado = Normalizer.normalize(resultado, Normalizer.Form.NFD);
        Pattern patron = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        resultado = patron.matcher(normalizado).replaceAll("");

        // 3. Reemplazar la ñ manualmente por n
        resultado = resultado.replace("ñ", "n").replace("Ñ", "N");

        // 4. Convertir a mayúsculas
        resultado = resultado.toUpperCase();

        // 5. Reemplazar espacios por guiones bajos
        resultado = resultado.replaceAll("\\s+", "_");

        // 6. Eliminar caracteres especiales (mantener solo letras, números y guión bajo)
        resultado = resultado.replaceAll("[^A-Z0-9_]", "");

        // 7. Concatenar el prefijo
        resultado = prefijo.toUpperCase() + resultado;

        // 8. Limitar a 100 caracteres
        if (resultado.length() > 100) {
            resultado = resultado.substring(0, 100);
        }

        return resultado;
    }

}
