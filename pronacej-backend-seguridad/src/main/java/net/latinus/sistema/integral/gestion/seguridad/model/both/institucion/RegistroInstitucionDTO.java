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
import net.latinus.sistema.integral.gestion.seguridad.model.both.JerarquiaDTO;
import net.latinus.sistema.integral.gestion.seguridad.service.LogService;
import net.latinus.sistema.integral.gestion.seguridad.utils.EtiquetaNemonico;
import net.latinus.sistema.integral.gestion.seguridad.entities.institucion.RegistroInstitucion;

import java.text.SimpleDateFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"idRegistroInstitucion"}, callSuper = true)
public class RegistroInstitucionDTO extends CamposDTO{
    private Long idRegistroInstitucion;
    private Long tokenFichaIdentificacion;
    private String nombreOrganizacion;
    private String nombreDirector;
    private String ruc;
    private String nombContactoOperacional;
    private String direccion;
    private String telefono;
    private String fax;
    private String email;
    private String sitioWeb;
    private String dni;
    private String misionInstitucional;
    private String objetivoInstitucional;
    private String departamento;
    private String servicios;
    private String beneficios;
    private String horariosServicios;
    private String serviciosArticulados;
    private String areaGeografica;
    private String participacionEspaciosLocales;
    private String otroSitioWeb;
    private CatalogoDTO tipoOrganizacion;
    private Boolean tieneConvenio;
    private String codigoUbigeoUbicacion;
    private String tipoInstitucion;
    private String finalidadInstitucion;
    private String estado;
    private JerarquiaDTO centro;


    public static CatalogoDTO entidadADtoCatalogo(Catalogo catalogo) {
        if (catalogo == null) {
            return null;
        }
        CatalogoDTO dto = new CatalogoDTO();
        dto.setIdCatalogo(catalogo.getIdCatalogo());
        dto.setNombre(catalogo.getNombre());
        dto.setDescripcion(catalogo.getDescripcion());
        dto.setNemonico(catalogo.getNemonico());
        dto.setCodigoExterno(catalogo.getCodigoExterno());
        dto.setTokenIdentificador(catalogo.getTokenIdentificador());
        dto.setTokenIdentificadorEmpresa(catalogo.getEmpresa().getTokenIdentificador());
        return dto;
    }

    public static Catalogo dtoAEntidadCatalogo(CatalogoDTO dto) {
        if (dto == null) {
            return null;
        }
        Catalogo entidad = new Catalogo();
        entidad.setIdCatalogo(dto.getIdCatalogo());
        entidad.setNombre(dto.getNombre());
        entidad.setDescripcion(dto.getDescripcion());
        entidad.setNemonico(dto.getNemonico());
        entidad.setCodigoExterno(dto.getCodigoExterno());
        entidad.setTokenIdentificador(dto.getTokenIdentificador());
        // Si la empresa está incluida en el DTO, conviértela aquí (opcional)
        return entidad;
    }


    // Método para convertir una entidad a un DTO
    public static RegistroInstitucionDTO entidadADtoRegistro(RegistroInstitucion registro) {
        if (registro == null) {
            return null;
        }
        RegistroInstitucionDTO dto = new RegistroInstitucionDTO();
        dto.setIdRegistroInstitucion(registro.getIdRegistroInstitucion());
        dto.setTokenIdentificador(registro.getTokenIdentificador());
        dto.setNombreOrganizacion(registro.getNombreOrganizacion());
        dto.setNombreDirector(registro.getNombreDirector());
        dto.setRuc(registro.getRuc());
        dto.setNombContactoOperacional(registro.getNombContactoOperacional());
        dto.setDireccion(registro.getDireccion());
        dto.setTelefono(registro.getTelefono());
        dto.setFax(registro.getFax());
        dto.setEmail(registro.getEmail());
        dto.setSitioWeb(registro.getSitioWeb());
        dto.setDni(registro.getDni());
        dto.setMisionInstitucional(registro.getMisionInstitucional());
        dto.setObjetivoInstitucional(registro.getObjetivoInstitucional());
        dto.setDepartamento(registro.getDepartamento());
        dto.setServicios(registro.getServicios());
        dto.setBeneficios(registro.getBeneficios());
        dto.setHorariosServicios(registro.getHorariosServicios());
        dto.setServiciosArticulados(registro.getServiciosArticulados());
        dto.setAreaGeografica(registro.getAreaGeografica());
        dto.setParticipacionEspaciosLocales(registro.getParticipacionEspaciosLocales());
        dto.setOtroSitioWeb(registro.getOtroSitioWeb());
        dto.setTipoOrganizacion(entidadADtoCatalogo(registro.getTipoOrganizacion()));
        dto.setCodigoUbigeoUbicacion(registro.getCodigoUbigeoUbicacion());
        dto.setTieneConvenio(registro.getTieneConvenio());
        dto.setFinalidadInstitucion(registro.getFinalidadInstitucion());
        return dto;
    }

    public static RegistroInstitucion dtoAEntidadRegistro(RegistroInstitucionDTO dto) {
        if (dto == null) {
            return null;
        }
        RegistroInstitucion entidad = new RegistroInstitucion();
        entidad.setIdRegistroInstitucion(dto.getIdRegistroInstitucion());
        entidad.setNombreOrganizacion(dto.getNombreOrganizacion());
        entidad.setNombreDirector(dto.getNombreDirector());
        entidad.setRuc(dto.getRuc());
        entidad.setNombContactoOperacional(dto.getNombContactoOperacional());
        entidad.setDireccion(dto.getDireccion());
        entidad.setTelefono(dto.getTelefono());
        entidad.setFax(dto.getFax());
        entidad.setEmail(dto.getEmail());
        entidad.setSitioWeb(dto.getSitioWeb());
        entidad.setDni(dto.getDni());
        entidad.setMisionInstitucional(dto.getMisionInstitucional());
        entidad.setObjetivoInstitucional(dto.getObjetivoInstitucional());
        entidad.setDepartamento(dto.getDepartamento());
        entidad.setServicios(dto.getServicios());
        entidad.setBeneficios(dto.getBeneficios());
        entidad.setHorariosServicios(dto.getHorariosServicios());
        entidad.setServiciosArticulados(dto.getServiciosArticulados());
        entidad.setAreaGeografica(dto.getAreaGeografica());
        entidad.setParticipacionEspaciosLocales(dto.getParticipacionEspaciosLocales());
        entidad.setOtroSitioWeb(dto.getOtroSitioWeb());
        entidad.setTipoOrganizacion(dtoAEntidadCatalogo(dto.getTipoOrganizacion()));
        entidad.setTieneConvenio(dto.getTieneConvenio());
        entidad.setCodigoUbigeoUbicacion(dto.getCodigoUbigeoUbicacion());
        entidad.setFinalidadInstitucion(dto.getFinalidadInstitucion());
        return entidad;
    }

}
