package net.latinus.sistema.integral.gestion.seguridad.utils;

import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.ParametroDelSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.param.ParametroDelSistemaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Service
@AllArgsConstructor
public class RSA {

    private ParametroDelSistemaRepository parametroDelSistemaRepository;

    private final String mdName = "SHA-256";
    private final String mgfName = "MGF1";
    private final String algorithm = "RSA";

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<String> en el body esta el texto encriptado
     *
     * @param texto String texto que se va a encriptar.
     * @param idEmpresa Long id de la empresa que va a realizar la encriptación.
     *
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> encriptarPorEmpresa(String texto, Long idEmpresa) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {
            Page<ParametroDelSistema> parametroDelSistemaPage = parametroDelSistemaRepository.findByNemonicoAndRemovido(
                    EtiquetaNemonico.PARAM_RSA_CLAVE_PUBLICA_FRONTEND, false,
                    PageRequest.of(0, 2, Sort.by("idParametroDelSistema").descending())
            );

            if (!parametroDelSistemaPage.hasContent()) {
                df.setMensaje("No se ha encontrado la clave de desencriptación");
                return df;
            }

            ParametroDelSistema parametroDelSistemaClaveRSAPublic = parametroDelSistemaPage.toList().getFirst();

            if (parametroDelSistemaClaveRSAPublic == null) {
                df.setMensaje("No se pudo encontrar la clave para realizar la encriptacion en RSA, consulta a tu administrador");
                return df;
            }

            String claveRSAPublica = parametroDelSistemaClaveRSAPublic.getValor();
            df.llenarRespuestaExitosa("Se encripto correctamente",
                    this.Encrypt(texto, claveRSAPublica));

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    /**
     * Devuelve un RespuestaPorDefectoAuditoria<String> en el body esta el texto desencriptado
     *
     * @param textoEncriptado String texto que se va a desencriptar.
     * @param idEmpresa Long id de la empresa que va a realizar la desencriptación.
     *
     * @return RespuestaPorDefectoAuditoria<String>
     */
    public RespuestaPorDefectoAuditoria<String> desencriptarPorEmpresa(String textoEncriptado, Long idEmpresa) {
        RespuestaPorDefectoAuditoria<String> df = new RespuestaPorDefectoAuditoria<>();

        try {
            Page<ParametroDelSistema> parametroDelSistemaPage = parametroDelSistemaRepository.findByNemonicoAndRemovido(
                    EtiquetaNemonico.PARAM_RSA_CLAVE_PRIVADA_BACKEND, false,
                    PageRequest.of(0, 2, Sort.by("idParametroDelSistema").descending())
            );

            if (!parametroDelSistemaPage.hasContent()) {
                df.setMensaje("No se ha encontrado la clave de desencriptación");
                return df;
            }

            ParametroDelSistema parametroDelSistemaClaveRSAPrivada = parametroDelSistemaPage.toList().getFirst();

            if (parametroDelSistemaClaveRSAPrivada == null) {
                df.setMensaje("No se pudo encontrar la clave rsa privada para realizar la desencriptación, consulta a tu administrador");
                return df;
            }

            String claveRsaPrivada = parametroDelSistemaClaveRSAPrivada.getValor();

            df.llenarRespuestaExitosa("Se desencripto correctamente",
                    this.Decrypt(textoEncriptado, claveRsaPrivada));

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    private PrivateKey createPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedPrivateKey = Base64.getDecoder().decode(key);

        KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        return privateKey;
    }

    private PublicKey createPublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] encodedPublicKey = Base64.getDecoder().decode(key);

        KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }

    public String Encrypt(String plain, String keyString) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        byte[] encryptedBytes;
        Cipher cipher = Cipher.getInstance(EtiquetaNemonico.SECURE_PADDING);
        keyString = keyString.replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("\\n", "")
                .replace("-----END PUBLIC KEY-----", "");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(this.mdName, this.mgfName,
                new MGF1ParameterSpec(this.mdName), PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, this.createPublicKey(keyString), oaepParams);
        encryptedBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);

    }

    public String Decrypt(String keyString, String textEncript) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(EtiquetaNemonico.SECURE_PADDING);
        keyString = keyString.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll("\\n", "")
                .replace("-----END RSA PRIVATE KEY-----", "");

        OAEPParameterSpec oaepParams = new OAEPParameterSpec(this.mdName, this.mgfName,
                new MGF1ParameterSpec(this.mdName), PSource.PSpecified.DEFAULT);

        cipher.init(Cipher.DECRYPT_MODE, this.createPrivateKey(keyString), oaepParams);


        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(textEncript));
        String value = new String(decryptedBytes);
        return value;
    }


    // Lee un PEM PKCS#8 y devuelve bytes DER
    public byte[] readPkcs8Pem(String pemTextPrivate) throws Exception {
        String b64 = pemTextPrivate.replaceAll("-----[^-]+-----", "").replaceAll("\\s+", "");
        return Base64.getDecoder().decode(b64);
    }

    private PrivateKey loadPrivateKeyPkcs8(String pemPath) throws Exception {
        byte[] der = this.readPkcs8Pem(pemPath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public String decryptBase64(List<String> chunkedBody, String pemTextPrivate) throws Exception {
        OAEPParameterSpec oaep256 = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );

        PrivateKey privateKey = this.loadPrivateKeyPkcs8(pemTextPrivate);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaep256);

        // Reconstruir el cuerpo desencriptado
        StringBuilder plaintextBuilder = new StringBuilder();

        for (String base64Chunk : chunkedBody) {
            byte[] ciphertext = Base64.getDecoder().decode(base64Chunk);
            byte[] plaintext = cipher.doFinal(ciphertext);
            plaintextBuilder.append(new String(plaintext, java.nio.charset.StandardCharsets.UTF_8));
        }

        return plaintextBuilder.toString();
    }

}
