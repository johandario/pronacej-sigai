package net.latinus.sistema.integral.gestion.seguridad.service.reporte;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.latinus.sistema.integral.gestion.seguridad.entities.Jerarquia;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyEncriptado;
import net.latinus.sistema.integral.gestion.seguridad.model.both.BodyJwtValido;
import net.latinus.sistema.integral.gestion.seguridad.model.both.reporte.AdolescenteExternadoDTO;
import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import net.latinus.sistema.integral.gestion.seguridad.model.response.RespuestaPorDefectoAuditoria;
import net.latinus.sistema.integral.gestion.seguridad.repository.param.ParametroDelSistemaRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.reporte.AdolescenteExternadoRepository;
import net.latinus.sistema.integral.gestion.seguridad.repository.seguridad.JerarquiaRepository;
import net.latinus.sistema.integral.gestion.seguridad.service.seguridad.JwtProviderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {
    private final AdolescenteExternadoRepository adolescenteExternadoRepository;

    private final JwtProviderService jwtProviderService;
    private final ParametroDelSistemaRepository parametroDelSistemaRepository;
    private final JerarquiaRepository jerarquiaRepository;

    @Override
    public RespuestaPorDefectoAuditoria<PaginacionResponse<AdolescenteExternadoDTO>> obtenerAdolescentesExternados(HttpServletRequest httpServletRequest, BodyEncriptado bodyEncriptado) {
        RespuestaPorDefectoAuditoria<PaginacionResponse<AdolescenteExternadoDTO>> df = new RespuestaPorDefectoAuditoria<>();

        try {

            RespuestaPorDefectoAuditoria<BodyJwtValido> df2 = this.jwtProviderService.obtenerBodyJwtApp(httpServletRequest);

            if (!df2.isExito()) {
                df.setMensaje(df2.getMensaje());
                df.setLogOut(true);
                return df;
            }

            RespuestaPorDefectoAuditoria<String> df22 = bodyEncriptado.desencriptarPorEmpresa(this.parametroDelSistemaRepository, null);
            if (!df22.isExito()) {
                df.setMensaje(df22.getMensaje());
                return df;
            }

            String bodyString = df22.getData();
            PaginacionRequest paginacionRequest = new Gson().fromJson(bodyString, PaginacionRequest.class);

            Pageable pageable = PageRequest.of(
                    paginacionRequest.getPage(),
                    paginacionRequest.getSize()
                    /*Sort.by(
                            Sort.Order.asc("nombreCompleto"),
                            Sort.Order.desc("fechaIngreso")
                    )*/
            );

            String valorFiltro = paginacionRequest.getFilter();
            String filtro = (valorFiltro == null || valorFiltro.trim().isEmpty())
                    ? null
                    : valorFiltro.trim();

            String valorCentro = "";
            String tokenCentro = paginacionRequest.getTokenIdentificador();
            if (tokenCentro != null && !tokenCentro.trim().isEmpty()) {
                Jerarquia jerarquiaCentro = this.jerarquiaRepository.findByTokenIdentificadorAndRemovido(tokenCentro, false);
                if (jerarquiaCentro != null) {
                    valorCentro = jerarquiaCentro.getNombre();
                }
            }
            String centro = (valorCentro == null || valorCentro.trim().isEmpty())
                    ? null
                    : valorCentro.trim();

            Page<AdolescenteExternadoDTO> adolescenteExternadoDTOPage = this.adolescenteExternadoRepository.obtenerDatosReporte(filtro, centro, pageable);

            PaginacionResponse<AdolescenteExternadoDTO> paginacionResponse = new PaginacionResponse<>();

            paginacionResponse.setData(adolescenteExternadoDTOPage.stream().toList());
            paginacionResponse.setTotalItems(adolescenteExternadoDTOPage.getTotalElements());

            long totalElementos = adolescenteExternadoDTOPage.getTotalElements(); // Total de roles que coinciden con el filtro
            long elementosPaginaActual = adolescenteExternadoDTOPage.getSize(); // Elementos en la página actual

            // Mensaje para el usuario - mostrar total de elementos
            String mensajeUsuario = "Se encontraron " + totalElementos + " registros que coinciden con el filtro '" + paginacionRequest.getFilter() + "', mostrando " + elementosPaginaActual + " en esta página";

            // Mensaje para auditoría
            String mensajeAuditoria = "Se han encontrado un total de " + totalElementos + " registros filtrados del sistema";

            df.llenarRespuestaExitosa(mensajeUsuario, paginacionResponse, mensajeAuditoria);

        } catch (Exception e) {
            df.llenarConDatosDeException(e);
        }

        return df;
    }
}
