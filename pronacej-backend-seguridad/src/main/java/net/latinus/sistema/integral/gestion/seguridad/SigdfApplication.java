package net.latinus.sistema.integral.gestion.seguridad;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;

@SpringBootApplication
@EnableScheduling
public class SigdfApplication {
    private final Environment env;

    @Autowired
    public SigdfApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(SigdfApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            private final LogService logService = new LogService(SigdfApplication.class);

            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                String cors = env.getProperty("cors");
                this.logService.info("Cors permitidos: " + cors);
                registry.addMapping("/**").allowedOrigins(cors);
            }
        };
    }
}
