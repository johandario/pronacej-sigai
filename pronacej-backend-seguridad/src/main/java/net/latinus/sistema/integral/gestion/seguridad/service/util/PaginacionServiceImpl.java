package net.latinus.sistema.integral.gestion.seguridad.service.util;

import net.latinus.sistema.integral.gestion.seguridad.model.request.PaginacionRequest;
import net.latinus.sistema.integral.gestion.seguridad.model.response.PaginacionResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaginacionServiceImpl implements PaginacionService {

    @Override
    public <T> PaginacionResponse<T> obtenerDatos(List<T> lista, PaginacionRequest paginacionRequest) {
        // Filtrar la lista si hay un filtro presente
        if (paginacionRequest.getFilter() != null && !paginacionRequest.getFilter().isEmpty()) {
            lista = lista.stream()
                    .filter(item -> contieneTerminoDeBusqueda(item, paginacionRequest.getFilter()))
                    .toList();
        }

        // Aplicar ordenamiento
        if (paginacionRequest.getSort() != null && !paginacionRequest.getSort().isEmpty()) {
            Sort.Order order = new Sort.Order(Sort.Direction.fromString(paginacionRequest.getDirection()), paginacionRequest.getSort());
            lista = lista.stream()
                    .sorted((a, b) -> compararPorCampo(a, b, order))
                    .toList();
        }

        // Aplicar paginación
        int start = Math.min(paginacionRequest.getPage() * paginacionRequest.getSize(), lista.size());
        int end = Math.min(start + paginacionRequest.getSize(), lista.size());
        List<T> paginatedList = lista.subList(start, end);

        PaginacionResponse<T> response = new PaginacionResponse<>();
        response.setData(paginatedList);
        response.setTotalItems((long) lista.size());

        return response;
    }

    // Método para filtrar un elemento por el searchTerm en sus campos de tipo String
    private <T> boolean contieneTerminoDeBusqueda(T item, String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (var field : getAllFields(item.getClass())) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(item);
                    if (value != null && value.toLowerCase().contains(lowerSearchTerm)) {
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // Método para ordenar la lista genéricamente
    private <T> int compararPorCampo(T a, T b, Sort.Order order) {
        try {
            var fieldA = getFieldRecursively(a.getClass(), order.getProperty());
            var fieldB = getFieldRecursively(b.getClass(), order.getProperty());
            fieldA.setAccessible(true);
            fieldB.setAccessible(true);

            Comparable valueA = (Comparable) fieldA.get(a);
            Comparable valueB = (Comparable) fieldB.get(b);

            return order.isAscending() ? valueA.compareTo(valueB) : valueB.compareTo(valueA);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private <T> Field getFieldRecursively(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
