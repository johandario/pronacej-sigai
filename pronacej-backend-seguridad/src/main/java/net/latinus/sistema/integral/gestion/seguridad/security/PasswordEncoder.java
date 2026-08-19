package net.latinus.sistema.integral.gestion.seguridad.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordEncoder {

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Devuelve un String con el text encriptado
     *
     * @param text BodyEncriptado objeto body encriptado.
     *
     * @return String
     */
    public String encode(String text) {
        return this.getPasswordEncoder().encode(text);
    }

    /**
     * Devuelve un Boolean true si las constraseñas coinciden o false si no
     *
     * @param text Stirng texto plano.
     * @param textEncoded String texto encriptado.
     *
     * @return Boolean
     */
    public Boolean matches(String text, String textEncoded) {
        return this.getPasswordEncoder().matches(text, textEncoded);
    }

}
