package net.latinus.sistema.integral.gestion.seguridad.service.param;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.AllArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Catalogo;
import net.latinus.sistema.integral.gestion.seguridad.entities.Localidad;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.Empresa;
import net.latinus.sistema.integral.gestion.seguridad.entities.seguridad.UsuarioSistema;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.LocalidadDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.CatalogoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.LocalidadRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Transactional
@AllArgsConstructor
public class LocalidadServiceImpl implements LocalidadService{

    private ParametroDelSistemaRepository parametroDelSistemaRepository;
    private LocalidadRepository localidadRepository;
    private JwtProviderService jwtProviderService;
    private CatalogoRepository catalogoRepository;

    @Override
    public RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerLocalidadPorNemonicPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            LocalidadDTO localidadDTO = new Gson().fromJson(body, LocalidadDTO.class);

            List<Localidad> listaLocalidades = this.localidadRepository.findByLocalidadPadre_NemonicoAndRemovido(localidadDTO.getNemonico(),false);

            List<LocalidadDTO> listaLocalidadesDTO = new ArrayList<>();

            for(Localidad localidad: listaLocalidades){
                LocalidadDTO localidadDTON = new LocalidadDTO();
                localidadDTON.setNemonico(localidad.getNemonico());
                localidadDTON.setNombre(localidad.getNombre());
                localidadDTON.setTipoLocalidad(localidad.getTipoLocalidad().getNemonico());
                localidadDTON.setTieneHijos(this.localidadRepository.existsByLocalidadPadre(localidad.getIdLocalidad()));
                localidadDTON.setTokenIdentificador(localidad.getTokenIdentificador());
                localidadDTON.setRutaUbigeo(localidad.getCodigoUbigeo());
                if(!ObjectUtils.isEmpty(localidad.getLocalidadPadre())){
                    localidadDTON.setTokenIdentificadorPadre(localidad.getLocalidadPadre().getTokenIdentificador());
                }
                listaLocalidadesDTO.add(localidadDTON);
            }

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + listaLocalidadesDTO.size() + " localidades disponibles. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + listaLocalidadesDTO.size() + " localidades por nemónico padre";

            df.llenarRespuestaExitosa(mensajeUsuario, listaLocalidadesDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;

    }

    @Override
    public RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerLocalidadPorNemonicTipo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();

            //catalogo Dto debe de ser el catalogo padre
            LocalidadDTO localidadDTO = new Gson().fromJson(body, LocalidadDTO.class);

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (Boolean.FALSE.equals(df2.isExito())) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            List<Localidad> listaLocalidades = this.localidadRepository.findByTipoLocalidad_NemonicoAndRemovido(localidadDTO.getTipoLocalidad(),false);

            List<LocalidadDTO> listaLocalidadesDTO = new ArrayList<>();

            for(Localidad localidad: listaLocalidades){
                LocalidadDTO localidadDTON = new LocalidadDTO();
                localidadDTON.setNemonico(localidad.getNemonico());
                localidadDTON.setNombre(localidad.getNombre());
                listaLocalidadesDTO.add(localidadDTON);
            }

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se han encontrado un total de " + listaLocalidadesDTO.size() + " localidades disponibles por tipo. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + listaLocalidadesDTO.size() + " localidades por tipo";

            df.llenarRespuestaExitosa(mensajeUsuario, listaLocalidadesDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<LocalidadDTO> obtenerLocalidadUbigeo(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {

        RespuestaPorDefectoAuditoria<LocalidadDTO> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            String ubigeo = new Gson().fromJson(bodyString, String.class);

            LocalidadDTO localidadDTO = new LocalidadDTO();
            Localidad localidad = this.localidadRepository.findByCodigoUbigeoAndRemovido(ubigeo,false);
            if(localidad==null){
                // Mensaje para el usuario
                String mensajeUsuario = "No existe una localidad con el codigo proporcionado: ubigeo-" + ubigeo;
                
                // Mensaje para auditoría
                String mensajeAuditoria = "Búsqueda fallida de localidad por ubigeo: " + ubigeo;

                df.llenarRespuestaExitosa(mensajeUsuario, localidadDTO, mensajeAuditoria);
                return df;
            }

            localidadDTO.setNemonico(localidad.getNemonico());
            localidadDTO.setRutaUbigeo(this.rutaUbigeo(localidad));
            localidadDTO.setNombre(localidad.getNombre());

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvo con éxito la localidad: " + localidad.getNombre() + ". Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se obtuvo con éxito la localidad por ubigeo: " + ubigeo;

            df.llenarRespuestaExitosa(mensajeUsuario, localidadDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerArbolPorNemonicPadre(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            LocalidadDTO localidadDTO = new Gson().fromJson(body, LocalidadDTO.class);

            Optional<Localidad> localidadOpt = localidadRepository.findLocalidadWithChildren(localidadDTO.getNemonico());

            if (localidadOpt.isEmpty()) {
                // Mensaje para el usuario
                String mensajeUsuario = "No se encontró localidad con nemónico: " + localidadDTO.getNemonico();
                
                // Mensaje para auditoría
                String mensajeAuditoria = "Búsqueda fallida de árbol de localidades por nemónico: " + localidadDTO.getNemonico();

                df.llenarRespuestaExitosa(mensajeUsuario, new ArrayList<LocalidadDTO>(), mensajeAuditoria);
                return df;
            }

            LocalidadDTO dtoFinal = mapearLocalidadAArbol(localidadOpt.get());

            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado un total de " + dtoFinal.getHijos().size() + " localidades en el árbol. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + dtoFinal.getHijos().size() + " localidades en árbol jerárquico";

            df.llenarRespuestaExitosa(mensajeUsuario, dtoFinal.getHijos(), mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<LocalidadDTO> obtenerLocalidadTokenIdentificador(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<LocalidadDTO> df = new RespuestaPorDefectoAuditoria<>();

        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String bodyString = df22.getData();
            String tokenIdentificador = new Gson().fromJson(bodyString, String.class);

            LocalidadDTO localidadDTO = new LocalidadDTO();
            Localidad localidad = this.localidadRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador,false);
            if(localidad==null){
                // Mensaje para el usuario
                String mensajeUsuario = "No existe una localidad con el codigo proporcionado: tokenIdentificador-" + tokenIdentificador;
                
                // Mensaje para auditoría
                String mensajeAuditoria = "Búsqueda fallida de localidad por token: " + tokenIdentificador;

                df.llenarRespuestaExitosa(mensajeUsuario, localidadDTO, mensajeAuditoria);
                return df;
            }

            localidadDTO.setNemonico(localidad.getNemonico());
            localidadDTO.setRutaUbigeo(this.rutaUbigeo(localidad));
            localidadDTO.setNombre(localidad.getNombre());
            localidadDTO.setUbigeo(localidad.getCodigoUbigeo());
            localidadDTO.setTokenIdentificador(localidad.getTokenIdentificador());
            localidadDTO.setTipoLocalidad(localidad.getTipoLocalidad() != null ? localidad.getTipoLocalidad().getNombre() : null);

            // Mensaje para el usuario
            String mensajeUsuario = "Se obtuvo con éxito la localidad: " + localidad.getNombre() + ". Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se obtuvo con éxito la localidad por token identificador: " + tokenIdentificador;

            df.llenarRespuestaExitosa(mensajeUsuario, localidadDTO, mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<List<LocalidadDTO>> obtenerDescendencia(HttpServletRequest httpServletRequest, String tokenIdentificador) {
        RespuestaPorDefectoAuditoria<List<LocalidadDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(df2.getLogOut());
                df.setSinAcceso(df2.getSinAcceso());
                return df;
            }

            BodyJwtValido bodyJwtValido = df2.getData();
            Empresa empresa = bodyJwtValido.getEmpresa();
            UsuarioSistema usuarioLogin = bodyJwtValido.getUsuarioSistema();
            df.setTokenIdentificadorEmpresa(empresa.getTokenIdentificador());

            Localidad localidad = this.localidadRepository.findByTokenIdentificadorAndRemovido(tokenIdentificador,
                    false);
            if (localidad == null) {
                df.setMensaje("No se ha encontrado la localidad solicitada o posiblemente esta ya fue eliminada");
                return df;
            }

            List<Localidad> localidadListDescendencia = new ArrayList<>();
            localidadListDescendencia.add(localidad);
            Localidad localidadPadre = localidad.getLocalidadPadre();
            while (localidadPadre != null) {
                localidadListDescendencia.add(localidadPadre);
                localidadPadre = localidadPadre.getLocalidadPadre();
            }

            Collections.reverse(localidadListDescendencia);

            List<LocalidadDTO> catalogoDTOList = localidadListDescendencia.stream().map(
                    (cat) -> {
                        LocalidadDTO localidadDTO = cat.convertirADTO();
                        Long cantidadDeHijos = this.localidadRepository.countByLocalidadPadreAndEmpresaAndRemovido(
                                cat,
                                empresa, false
                        );
                        localidadDTO.setTieneHijos(cantidadDeHijos > 0);

                        return localidadDTO;
                    }
            ).toList();

            // Mensaje para el usuario
            String mensajeUsuario = "Se han encontrado un total de " + catalogoDTOList.size() + " descendientes disponibles. Consulta realizada por: " +
                    usuarioLogin.getUserName() + " con identificación: " + usuarioLogin.getNumeroDeDocumento() + "(" + usuarioLogin.getTokenIdentificador() + ")";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + catalogoDTOList.size() + " descendientes de localidad";

            df.llenarRespuestaExitosa(mensajeUsuario, catalogoDTOList, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<LocalidadDTO> crearLocalidad(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<LocalidadDTO> df = new RespuestaPorDefectoAuditoria<>();
        try{

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }
            String body = df22.getData();
            LocalidadDTO localidadDTO = new Gson().fromJson(body, LocalidadDTO.class);

            Optional<Localidad> optionalLocalidad = this.localidadRepository.findByCodigoUbigeoAndRemovidoFalse(localidadDTO.getUbigeo());

            if(optionalLocalidad.isPresent()){
                df.setMensaje("Ya existe una localidad con el codigo ubigeo: " + localidadDTO.getUbigeo());
                return df;
            }

            Optional<Localidad> localidadOpt1 = this.localidadRepository.findByNemonicoAndRemovidoFalse(localidadDTO.getNemonico());

            if(localidadOpt1.isPresent()){
                df.setMensaje("Ya existe una localidad con el código ubigeo: " + localidadDTO.getUbigeo());
                return df;
            }

            Localidad localidad = new Localidad();
            localidad.setNemonico(localidadDTO.getNemonico());
            localidad.setLocalidadPadre(this.localidadRepository.findByTokenIdentificadorAndRemovido(localidadDTO.getTokenIdentificadorPadre(), false));
            localidad.setNombre(localidadDTO.getNombre());
            localidad.setTipoLocalidad(this.catalogoRepository.findByNemonicoAndRemovido(localidadDTO.getNemonicoTipoLocalidad(), false));
            Catalogo catalogoTipo = this.catalogoRepository.findByNemonicoAndRemovido(localidadDTO.getTipoLocalidad(), false);
            if (catalogoTipo == null) {
                System.out.println("❌ No se encontró catálogo con nemónico: " + localidadDTO.getTipoLocalidad());
            } else {
                System.out.println("✅ Catálogo encontrado: " + catalogoTipo.getIdCatalogo() + " - " + catalogoTipo.getNombre());
            }

            localidad.setTipoLocalidad(catalogoTipo);
            localidad.setCodigoUbigeo(localidadDTO.getUbigeo());
            localidad.setRemovido(false);

            this.localidadRepository.save(localidad);

            System.out.println("➡️ LocalidadDTO recibido:");
            System.out.println("Nombre: " + localidadDTO.getNombre());
            System.out.println("Nemonico: " + localidadDTO.getNemonico());
            System.out.println("Ubigeo: " + localidadDTO.getUbigeo());
            System.out.println("Token padre: " + localidadDTO.getTokenIdentificadorPadre());
            System.out.println("Tipo localidad (nemonico): " + localidadDTO.getTipoLocalidad());

            // Obtener datos para el mensaje
            String nombreUsuarioResponsable = obtenerNombreCompletoUsuarioSistema(usuarioLogin);
            Date fechaAccion = new Date();
            String fechaFormateada = formatearFechaEspanol(fechaAccion);

            // Mensaje original para el usuario (mantener simple)
            String mensajeUsuario = "Se ha creado la localidad correctamente: " + localidad.getNombre();

            // Mensaje para auditoría (nuevo formato)
            String mensajeAuditoria = "Se creó con éxito la localidad " + localidad.getNombre() + 
                                    " del " + fechaFormateada + " por el usuario " + nombreUsuarioResponsable;

            df.llenarRespuestaExitosa(mensajeUsuario, localidad.convertirADTO(), mensajeAuditoria);

        }catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }
        return df;

    }

    private String rutaUbigeo(Localidad localidad){
        if(localidad.getLocalidadPadre() == null){
            return localidad.getNombre();
        }
        return  rutaUbigeo(localidad.getLocalidadPadre()) +"/"+ localidad.getNombre();
    }

    private LocalidadDTO mapearLocalidadAArbol(Localidad localidad) {
        LocalidadDTO dto = new LocalidadDTO();
        dto.setNombre(localidad.getNombre());
        dto.setNemonico(localidad.getNemonico());
//        dto.setTipoLocalidad(localidad.getTipoLocalidad() != null ? localidad.getTipoLocalidad().getNombre() : null);
        dto.setRutaUbigeo(localidad.getCodigoUbigeo());

        // Recursivamente asignar hijos
        List<Localidad> hijos = localidadRepository.findByLocalidadPadre(localidad);
        for (Localidad hijo : hijos) {
            dto.getHijos().add(mapearLocalidadAArbol(hijo));
        }

        return dto;
    }

    /**
     * Formatea una fecha al español en el formato: "viernes, 30 de mayo del 2025"
     */
    private String formatearFechaEspanol(Date fecha) {
        if (fecha == null) {
            return "fecha no disponible";
        }

        try {
            // Configurar el locale para español
            Locale localeEspanol = new Locale("es", "ES");

            // Crear el formato personalizado
            SimpleDateFormat formatoCompleto = new SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy", localeEspanol);

            return formatoCompleto.format(fecha);
        } catch (Exception e) {
            // En caso de error, devolver un formato simple
            SimpleDateFormat formatoSimple = new SimpleDateFormat("dd/MM/yyyy");
            return formatoSimple.format(fecha);
        }
    }

    /**
     * Método auxiliar para obtener nombres completos de un UsuarioSistema
     */
    private String obtenerNombreCompletoUsuarioSistema(UsuarioSistema usuario) {
        if (usuario == null) {
            return "N/A";
        }

        StringBuilder nombreCompleto = new StringBuilder();
        if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
            nombreCompleto.append(usuario.getNombres());
        }
        if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(usuario.getApellidos());
        }

        return nombreCompleto.length() > 0 ? nombreCompleto.toString() : "N/A";
    }


    @Override
    public RespuestaPorDefectoAuditoria<LocalidadDTO> obtenerLocalidadPorNemonico(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<LocalidadDTO> df = new RespuestaPorDefectoAuditoria<>();

        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String body = df22.getData();
            String nemonico = new Gson().fromJson(body, String.class);

            Localidad localidad = this.localidadRepository.findByNemonicoAndRemovido(nemonico, false);
            if (localidad == null) {
                df.setMensaje("No se encontró ninguna localidad con el nemónico proporcionado: " + nemonico);
                return df;
            }

            LocalidadDTO localidadDTO = localidad.convertirADTO();

            String mensajeUsuario = "Se encontró la localidad " + localidad.getNombre() + " con nemónico " + nemonico + ".";
            String mensajeAuditoria = "Consulta exitosa de localidad por nemónico: " + nemonico;

            df.llenarRespuestaExitosa(mensajeUsuario, localidadDTO, mensajeAuditoria);

        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }

    @Override
    public RespuestaPorDefectoAuditoria<LocalidadDTO> editarLocalidad(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<LocalidadDTO> df = new RespuestaPorDefectoAuditoria<>();
        try {
            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);
            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }
            UsuarioSistema usuarioLogin = df2.getData().getUsuarioSistema();

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String body = df22.getData();
            LocalidadDTO localidadDTO = new Gson().fromJson(body, LocalidadDTO.class);

            Localidad localidad = this.localidadRepository.findByTokenIdentificadorAndRemovido(localidadDTO.getTokenIdentificador(), false);
            if (localidad == null) {
                df.setMensaje("No se encontró la localidad a editar.");
                return df;
            }
//             Validaciones básicas si deseas evitar duplicados
//          Optional<Localidad> localidadUbigeo = this.localidadRepository.findByCodigoUbigeoAndRemovidoFalse(localidadDTO.getUbigeo());
//            if (localidadUbigeo.isPresent() && !localidadUbigeo.get().getIdLocalidad().equals(localidad.getIdLocalidad())) {
//                df.setMensaje("Ya existe otra localidad con el mismo código ubigeo.");
//                return df;
//            }

            Optional<Localidad> localidadNemonico = this.localidadRepository.findByNemonicoAndRemovidoFalse(localidadDTO.getNemonico());
            if (localidadNemonico.isPresent() && !localidadNemonico.get().getIdLocalidad().equals(localidad.getIdLocalidad())) {
                df.setMensaje("Ya existe otra localidad con el mismo nemónico.");
                return df;
            }

            // Actualización de campos permitidos
            localidad.setNombre(localidadDTO.getNombre());
            localidad.setNemonico(localidadDTO.getNemonico());


            this.localidadRepository.save(localidad);

            // Respuesta
            String mensajeUsuario = "La localidad fue editada correctamente: " + localidad.getNombre();
            String mensajeAuditoria = "Se editó la localidad con ID " + localidad.getIdLocalidad() + " por el usuario " +
                    usuarioLogin.getUserName() + " con ID " + usuarioLogin.getNumeroDeDocumento();

            df.llenarRespuestaExitosa(mensajeUsuario, localidad.convertirADTO(), mensajeAuditoria);
        } catch (Exception ex) {
            df.llenarConDatosDeException(ex);
        }

        return df;
    }
}