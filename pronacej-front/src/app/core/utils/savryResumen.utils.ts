import { ContestacionDTO } from 'app/core/model/both/encuesta/contestacionDTO.model';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import { PreguntaDTO } from 'app/core/model/both/encuesta/preguntaDTO.model';
import { SeccionDTO } from 'app/core/model/both/encuesta/seccionDTO.model';

export interface SavryGrupoResumen {
    grupo: string;
    bajo: number;
    medio: number;
    alto: number;
    presente: number;
    ausente: number;
    criticos: number;
}

export interface SavryDetalleItem {
    grupo: string;
    numero: number;
    pregunta: string;
    valor: string;
    critico: boolean;
}

export type SavryNivelClave = 'bajo' | 'medio' | 'alto' | 'presente' | 'ausente';

/** Quita HTML y normaliza texto de opción de respuesta. */
export function limpiarTextoRespuesta(texto: string | null | undefined): string {
    if (!texto) {
        return '';
    }
    return texto
        .replace(/<[^>]+>/g, ' ')
        .replace(/&nbsp;/gi, ' ')
        .replace(/\s+/g, ' ')
        .trim();
}

/**
 * Clasifica una opción SAVRY en Bajo/Medio/Alto/Presente/Ausente.
 */
export function clasificarNivelSavry(textoOpcion: string | null | undefined): SavryNivelClave | null {
    const t = limpiarTextoRespuesta(textoOpcion).toUpperCase();
    if (!t) {
        return null;
    }
    if (/\bBAJO\b/.test(t) || t === 'B') {
        return 'bajo';
    }
    if (/\bMEDIO\b/.test(t) || t === 'M') {
        return 'medio';
    }
    if (/\bALTO\b/.test(t) || t === 'A') {
        return 'alto';
    }
    if (/\bPRESENTE\b/.test(t) || t === 'P') {
        return 'presente';
    }
    if (/\bAUSENTE\b/.test(t) || t === 'N' || t === 'AUS') {
        return 'ausente';
    }
    return null;
}

function etiquetaValorMostrar(nivel: SavryNivelClave | null, textoOriginal: string): string {
    if (nivel === 'bajo') return 'BAJO';
    if (nivel === 'medio') return 'MEDIO';
    if (nivel === 'alto') return 'ALTO';
    if (nivel === 'presente') return 'PRESENTE';
    if (nivel === 'ausente') return 'AUSENTE';
    return limpiarTextoRespuesta(textoOriginal).toUpperCase() || '—';
}

function obtenerContestacionPregunta(pregunta: PreguntaDTO): ContestacionDTO | undefined {
    if (pregunta.contestaciones?.length) {
        return pregunta.contestaciones[0];
    }
    return undefined;
}

function resolverTextoYCritico(
    pregunta: PreguntaDTO,
    contestacionOverride?: { idRespuesta?: number; critico?: boolean }
): { valorTexto: string; critico: boolean; nivel: SavryNivelClave | null } {
    const contestacion = contestacionOverride
        ? ({
              idRespuesta: contestacionOverride.idRespuesta,
              critico: contestacionOverride.critico,
          } as ContestacionDTO)
        : obtenerContestacionPregunta(pregunta);

    const idResp = contestacion?.idRespuesta;
    const respuesta = pregunta.respuestas?.find((r) => r.idRespuesta === idResp);
    const valorTexto = respuesta?.respuesta || '';
    const nivel = clasificarNivelSavry(valorTexto);
    const critico = Boolean(contestacion?.critico);
    return { valorTexto, critico, nivel };
}

/**
 * Resumen por grupo (sección) a partir de una encuesta ya cargada con contestaciones.
 */
export function calcularResumenSavryDesdeEncuesta(evaluacion: EncuestaDTO | null | undefined): SavryGrupoResumen[] {
    if (!evaluacion?.secciones?.length) {
        return [];
    }

    return evaluacion.secciones.map((seccion: SeccionDTO) => {
        const fila: SavryGrupoResumen = {
            grupo: seccion.nombre || '',
            bajo: 0,
            medio: 0,
            alto: 0,
            presente: 0,
            ausente: 0,
            criticos: 0,
        };

        (seccion.preguntas || []).forEach((pregunta) => {
            const { nivel, critico } = resolverTextoYCritico(pregunta);
            if (nivel) {
                fila[nivel] += 1;
            }
            if (critico) {
                fila.criticos += 1;
            }
        });

        return fila;
    });
}

/**
 * Detalle de respuestas por grupo para el PDF (sección III).
 */
export function calcularDetalleSavryDesdeEncuesta(evaluacion: EncuestaDTO | null | undefined): SavryDetalleItem[] {
    if (!evaluacion?.secciones?.length) {
        return [];
    }

    const items: SavryDetalleItem[] = [];
    evaluacion.secciones.forEach((seccion) => {
        let n = 0;
        (seccion.preguntas || []).forEach((pregunta) => {
            n += 1;
            const { valorTexto, critico, nivel } = resolverTextoYCritico(pregunta);
            items.push({
                grupo: seccion.nombre || '',
                numero: n,
                pregunta: limpiarTextoRespuesta(pregunta.texto || ''),
                valor: etiquetaValorMostrar(nivel, valorTexto),
                critico,
            });
        });
    });
    return items;
}

/**
 * Resumen desde el FormGroup de evaluación (al completar, antes de persistir).
 * `obtenerIdRespuestaYCritico(sectionIndex, questionIndex)` debe devolver lo marcado en el form.
 */
export function calcularResumenSavryDesdeFormulario(
    evaluacion: EncuestaDTO | null | undefined,
    obtenerRespuesta: (
        sectionIndex: number,
        questionIndex: number
    ) => { idRespuesta?: number; critico?: boolean }
): SavryGrupoResumen[] {
    if (!evaluacion?.secciones?.length) {
        return [];
    }

    return evaluacion.secciones.map((seccion, sIdx) => {
        const fila: SavryGrupoResumen = {
            grupo: seccion.nombre || '',
            bajo: 0,
            medio: 0,
            alto: 0,
            presente: 0,
            ausente: 0,
            criticos: 0,
        };

        (seccion.preguntas || []).forEach((pregunta, pIdx) => {
            const override = obtenerRespuesta(sIdx, pIdx);
            const { nivel, critico } = resolverTextoYCritico(pregunta, override);
            if (nivel) {
                fila[nivel] += 1;
            }
            if (critico) {
                fila.criticos += 1;
            }
        });

        return fila;
    });
}

export function esEncuestaFactoresRiesgo(evaluacion: EncuestaDTO | null | undefined): boolean {
    if (!evaluacion) {
        return false;
    }
    const tokens = [
        evaluacion.nemonico,
        evaluacion.nemonicoCategoria,
        (evaluacion as any).nemonicoCatalogo,
    ]
        .filter(Boolean)
        .map((x) => String(x).toUpperCase());

    if (tokens.some((t) => t.includes('FACTORES_DE_RIESGO') || t.includes('SAVRY'))) {
        return true;
    }
    // Heurística: 4 grupos típicos SAVRY
    const nombres = (evaluacion.secciones || []).map((s) => (s.nombre || '').toUpperCase());
    const tieneHistoricos = nombres.some((n) => n.includes('HISTÓRIC') || n.includes('HISTORIC'));
    const tieneProteccion = nombres.some((n) => n.includes('PROTECCIÓN') || n.includes('PROTECCION'));
    return tieneHistoricos && tieneProteccion;
}
