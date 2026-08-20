import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import { Chart, registerables } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { EncuestaDTO } from 'app/core/model/both/encuesta/encuestaDTO.model';
import etiquetasModel from 'app/core/etiquetas.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { EncuestaService } from './encuesta.service';
import { EncabezadoDTO } from 'app/core/model/both/encuesta/encabezadoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import Theme from 'quill/core/theme';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import {
    calcularDetalleSavryDesdeEncuesta,
    calcularResumenSavryDesdeEncuesta,
    esEncuestaFactoresRiesgo,
    SavryDetalleItem,
    SavryGrupoResumen,
} from 'app/core/utils/savryResumen.utils';

Chart.register(...registerables, ChartDataLabels);

@Injectable({
    providedIn: 'root',
})
export class EncuestaPdfService {

    nemonicoMenu = etiquetasModel.NEMONICO_MENU_USUARIO;

    constructor(
        private dialogMensajeService: DialogMensajeService,
        private encuestaService: EncuestaService,
        private funcionesUtils: FuncionesUtils
    ) { }


    /**
     * Genera un gráfico circular en formato Base64.
     * @param totalGeneral Valor total del gráfico.
     * @param callback Función de retorno con el gráfico en Base64.
     */
    generarGraficoResumen(
        totalesPorRespuesta: Record<string, number>,
        callback: (imgData: string) => void
    ): void {
        const canvas = document.createElement('canvas');
        canvas.width = 800;
        canvas.height = 400;
        const ctx = canvas.getContext('2d');

        if (ctx) {
            const labels = Object.keys(totalesPorRespuesta);
            const data = Object.values(totalesPorRespuesta);

            const colores = [
                '#3498DB', // Azul
                '#2ECC71', // Verde
                '#F1C40F', // Amarillo
                '#E67E22', // Naranja
                '#E74C3C', // Rojo
                '#9B59B6', // Morado
                '#1ABC9C', // Turquesa
                '#95A5A6', // Gris
            ];

            const backgroundColors = labels.map((_, index) => colores[index % colores.length]);

            const chart = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels,
                    datasets: [
                        {
                            data,
                            backgroundColor: backgroundColors,
                            borderWidth: 1,
                            hoverOffset: 4,
                        },
                    ],
                },
                options: {
                    responsive: false,
                    cutout: '60%',
                    layout: {
                        padding: 20,
                    },
                    plugins: {
                        legend: {
                            display: true,
                            position: 'right',
                            labels: {
                                font: {
                                    size: 14,
                                },
                            },
                        },
                        tooltip: {
                            callbacks: {
                                label: function (context) {
                                    const label = context.label || '';
                                    const value = context.raw || 0;
                                    return `${label}: ${value}`;
                                },
                            },
                        },
                    },
                    animation: {
                        onComplete: () => {
                            const imgData = canvas.toDataURL('image/png');
                            callback(imgData || null);
                        },
                    },
                },
            });
        }
    }


    /**
     * Genera un gráfico de radar en formato Base64.
     * @param totales Valores de las secciones.
     * @param etiquetas Etiquetas de las secciones.
     * @param callback Función de retorno con el gráfico en Base64.
     */
    generarGraficoRadar(totales: number[], etiquetas: string[], callback: (imgData: string) => void): void {
        const canvas = document.createElement('canvas');
        canvas.width = 500;
        canvas.height = 500;
        const ctx = canvas.getContext('2d');

        if (ctx) {

            // Generar el gráfico usando Chart.js
            new Chart(ctx, {
                type: 'radar',
                data: {
                    labels: etiquetas,
                    datasets: [
                        {
                            data: totales,
                            backgroundColor: 'rgba(255, 165, 0, 0.2)',
                            borderColor: '#FFA500',
                        },
                    ],
                },
                options: {
                    responsive: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: { enabled: false }
                        //datalabels: { display: false } // <--- 🔥 evita que se impriman los valores
                    },
                    scales: {
                        r: {
                            min: Math.min(...totales) - 1,
                            max: Math.max(...totales) + 1,
                            pointLabels: {
                                font: {
                                    size: 11 // ← Tamaño del texto de etiquetas (dominios)
                                }
                            },
                            ticks: {
                                font: {
                                    size: 8 // ← Tamaño del texto de los números (eje radial)
                                }
                            }
                        },
                    },
                },
            });

            // Convertir directamente a Base64
            setTimeout(() => {
                const imgData = canvas.toDataURL('image/png');
                callback(imgData || null);
            }, 500);
        }
    }

    generarGraficoBarras(totales: number[], etiquetas: string[], colores: string[], callback: (imgData: string | null) => void) {
        const canvas = document.createElement('canvas');
        canvas.width = 800;
        canvas.height = 800;
        const ctx = canvas.getContext('2d');

        if (!ctx) {
            callback(null);
            return;
        }

        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: etiquetas,
                datasets: [{
                    label: 'Totales',
                    data: totales,
                    backgroundColor: colores,
                    borderColor: colores.map(c => c.replace('0.8', '1')), // Bordes más oscuros
                    borderWidth: 1
                }]
            },
            options: {
                responsive: false,
                animation: false,
                scales: {
                    y: {
                        beginAtZero: true
                    },
                    x: {
                        ticks: {
                            color: '#000',  // Establece el color de las etiquetas del eje x a negro
                            font: {
                                family: 'Helvetica',
                                size: 16,
                                weight: 'bold'
                            },
                            maxRotation: 90,
                            minRotation: 90
                        }
                    }
                },
                plugins: {
                    legend: { display: false, },
                    tooltip: { enabled: true },
                    datalabels: {
                        color: '#fff',           // Color blanco
                        anchor: 'end',
                        align: 'start',
                        font: {
                            weight: 'bold',
                            size: 14
                        },
                        formatter: (value) => value
                    }
                }
            }
        });

        // Espera un pequeño delay para que Chart.js renderice
        setTimeout(() => {
            const imgData = canvas.toDataURL('image/png');
            callback(imgData || null);
        }, 500);
    }

    /**
     * Genera un PDF basado en una evaluación.
     * @param evaluacion Objeto de tipo EncuestaDTO.
     * @returns El contenido del PDF en formato Base64.
     */
    generarPDF(encabezado: EncabezadoDTO, evaluacion: EncuestaDTO, abrirEnNavegador: boolean = true): Promise<string> {
        console.log(evaluacion);

        if (esEncuestaFactoresRiesgo(evaluacion)) {
            return this.generarPdfSavry(encabezado, evaluacion, abrirEnNavegador);
        }

        return new Promise((resolve) => {
            const self = this;

            let base64Content = 'no content';

            const primary = "00a3e2";  //[0, 163, 226]
            const secondary = "0090d4"; //[0, 144, 212]
            const accent = "484b54"; //[72, 75, 84]
            const accent300 = "cbd5e1"; //[203, 213, 225]
            const contrast = "e62984"; //[203, 213, 225]
            const message = "cad200"; //[202, 210, 0]

            const logo = new Image();
            logo.src = 'images/logo/logo.png';

            const doc = new jsPDF();

            logo.onload = () => {
                // -------------------------------
                // CALCULOS PARA LA TABLA GENERAL
                // -------------------------------
                const seccionesTotales = new Map<string, number>(); // Guardar la cantidad de preguntas por sección

                evaluacion.secciones.forEach((seccion) => {
                    const cantidadPreguntas = seccion.preguntas.length;
                    seccionesTotales.set(seccion.nombre, cantidadPreguntas);
                });

                const generalData1 = [];
                let totalGeneral = 0;

                generalData1.push(['Sección', 'Preguntas']);

                // Utilizar los valores acumulados guardados para cada sección
                evaluacion.secciones.forEach((seccion) => {
                    const cantidadPreguntas = seccion.preguntas.length;
                    totalGeneral += cantidadPreguntas;

                    // Agregar solo nombre de la sección y su total de riesgo
                    generalData1.push([
                        seccion.nombre,
                        cantidadPreguntas
                    ]);
                });

                // Agregar total general
                generalData1.push([
                    'Total',
                    totalGeneral
                ]);

                // -------------------------------
                // LOGO
                // -------------------------------

                const pageWidth = doc.internal.pageSize.getWidth();
                const logoWidth = 60; // ancho deseado en mm
                const logoHeight = 18; // alto deseado
                const logoX = 5;
                const logoY = 5;

                const tituloX = (pageWidth / 2);

                doc.addImage(logo, 'PNG', logoX, logoY, logoWidth, logoHeight);

                const maxTextWidth = pageWidth - logoWidth * 2; // el ancho disponible
                const splittedTitle = doc.splitTextToSize(encabezado.nombre, maxTextWidth);

                doc.setFont('helvetica', 'bold');
                const fontSize = 14;
                doc.setFontSize(fontSize);

                const lineHeight = fontSize * 0.35;

                const titleHeight = splittedTitle.length * lineHeight;

                doc.text(splittedTitle, tituloX, logoY + logoHeight + lineHeight, {
                    align: 'center'
                });

                // doc.setFont('helvetica', 'bold');
                // doc.text(encabezado.nombre, tituloX, logoHeight);

                // Mover el startY para dejar espacio debajo del logo
                let startY = logoY + logoHeight + titleHeight + 5;

                const rectX = 20;
                const rectY = startY;
                const rectWidth = pageWidth - 40;
                const rectHeight = 10;

                const infoText = "INFORMACIÓN GENERAL";
                const infoTextWidth = doc.getTextWidth(infoText);
                const infoTextX = rectX + (rectWidth - infoTextWidth) / 2;
                const infoTextY = rectY + rectHeight - 3;

                // Rectángulo azul de título
                doc.setFillColor(primary); // Fondo azul oscuro
                doc.rect(rectX, rectY, rectWidth, rectHeight, 'F'); // Rellenar
                doc.setDrawColor(primary); // Borde del mismo color azul

                // Texto blanco centrado
                doc.setFontSize(12);
                doc.setTextColor(255, 255, 255);
                doc.setFont('helvetica', 'bold');
                doc.text(infoText, infoTextX, infoTextY);

                startY = rectY + rectHeight + 5;

                // -------------------------------
                // TABLA DE INFORMACIÓN GENERAL
                // -------------------------------
                const infoData = [
                    ['Nombre:', evaluacion.nombre.toUpperCase() ?? ''],
                    ['Descripción:', evaluacion.descripcion.toUpperCase() ?? ''],
                    ['Tipo Centro:', evaluacion.tipoCentro.toUpperCase() ?? ''],
                    ['Categoría:', evaluacion?.categoria.toUpperCase() ?? ''],
                    ['Adolescente:', evaluacion?.adolescente?.toUpperCase() ?? ''],
                    ['DNI:', evaluacion?.dniAdolescente?.toUpperCase() ?? ''],
                    ['Fecha de Creación:', this.funcionesUtils.formatearFechaHora(encabezado?.fechaCreacion) ?? ''],
                    ['Fecha de Finalización:', this.funcionesUtils.formatearFechaHora(encabezado?.fechaCompletacion) ?? ''],
                ];


                autoTable(doc, {
                    startY: startY,
                    body: infoData,
                    theme: 'grid',
                    styles: {
                        fontSize: 10,
                        cellPadding: 3,
                        valign: 'middle',
                        halign: 'left',
                        font: 'helvetica',
                        lineColor: [0, 163, 226],
                        lineWidth: 0.1
                    },
                    headStyles: {
                        fillColor: [44, 62, 80], // AZUL OSCURO
                        textColor: 255,
                        fontStyle: 'bold',
                        lineColor: [0, 0, 0],
                        lineWidth: 0.5
                    },
                    bodyStyles: {
                        fillColor: [255, 255, 255],
                        textColor: 0,
                        lineColor: [0, 163, 226],
                        lineWidth: 0.5
                    },
                    columnStyles: {
                        0: { fontStyle: 'bold', cellWidth: 55, fillColor: [203, 213, 225] }, // Amarillo
                        1: { cellWidth: 'auto' }
                    },
                    didParseCell: (data) => {
                        if (data.column.index === 0) {
                            // Establecer color amarillo (como en imagen deseada)
                            data.cell.styles.fillColor = [203, 213, 225];
                            data.cell.styles.fontStyle = 'bold';
                            data.cell.styles.textColor = 0; // Texto negro
                        }
                    }
                });

                // Actualizar posición para el siguiente bloque 
                startY = (doc as any).lastAutoTable.finalY + 15;

                // -------------------------------
                // TABLA DE SECCIONES
                // -------------------------------
                if (startY + 80 > doc.internal.pageSize.getHeight()) {
                    doc.addPage();
                    startY = 20;
                }

                // Obtener datos de las secciones (totales y etiquetas)
                const etiquetas: string[] = [];
                const totales: number[] = [];

                evaluacion.secciones.forEach((seccion) => {

                    let nombre = seccion.nombre;
                    if (nombre.length > 20) {
                        nombre = nombre.slice(0, 30) + '...';
                    }
                    etiquetas.push(nombre);
                    totales.push(seccion.preguntas.length); // Total es la cantidad de preguntas
                });

                etiquetas.push()

                // Verificar si etiquetas.length > 2 antes de aplicar las propiedades
                const tableConfig = { margin: { left: 10 }, tableWidth: 70 };

                const colores = generarColores(etiquetas.length);

                // Mostrar tabla de secciones
                autoTable(doc, {
                    startY: startY,
                    ...tableConfig,
                    body: generalData1,
                    theme: 'grid',
                    styles: {
                        fontSize: 10,
                        cellPadding: 3,
                        valign: 'middle',
                        halign: 'center',
                        font: 'helvetica',
                        lineColor: [0, 0, 0],
                        lineWidth: 0.1
                    },
                    headStyles: {
                        fillColor: [0, 163, 226],
                        textColor: 255,
                        fontStyle: 'bold',
                        lineColor: [0, 163, 226],
                        lineWidth: 0.1,
                        halign: 'center'
                    },
                    bodyStyles: {
                        fillColor: [255, 255, 255],
                        textColor: 0,
                        lineColor: [0, 163, 226],
                        lineWidth: 0.5
                    },
                    columnStyles: {
                        0: { fontStyle: 'bold', halign: 'left' }
                    },
                    didParseCell: (data) => {
                        if (data.row.index > 0 && data.column.index === 0 && data.row.index - 1 < colores.length) {
                            console.log(data.row.index);
                            const color = rgbaStringToRgbArray(colores[data.row.index - 1]);
                            data.cell.styles.fillColor = color;
                            data.cell.styles.textColor = 255;
                        }

                        // Hacer negrita la última fila (totales)
                        if (data.row.index === generalData1.length - 1) {
                            data.cell.styles.fontStyle = 'bold';
                        }

                        // Hacer negrita la primera fila
                        if (data.row.index === 0) {
                            data.cell.styles.fontStyle = 'bold';
                            data.cell.styles.halign = 'center';
                        }
                    }
                });

                // -------------------------------
                //INSERTAR EL GRAFICO BARRAS
                // -------------------------------

                if (etiquetas.length > 1) {
                    // Generar el gráfico de radar
                    let rectX = 80; // Desplazamiento a la derecha
                    const chartWidth = 120;
                    const chartHeight = 100;
                    startY += 10; // Desplazamiento hacia abajo

                    this.generarGraficoBarras(totales, etiquetas, colores, (imgData) => {
                        if (imgData) {
                            const pageHeight = doc.internal.pageSize.getHeight();
                            if (startY + chartHeight > pageHeight) {
                                doc.addPage();
                                startY = 20;
                            }
                            doc.addImage(imgData, 'PNG', rectX, startY, chartWidth, chartHeight);

                            // Aumentamos Y solo si vas a seguir agregando contenido debajo
                            const maxY = Math.max(
                                (doc as any).lastAutoTable.finalY,
                                startY + chartHeight
                            );

                            rectX = 20; // Reiniciar X para el siguiente bloque

                            continuarPDF(maxY + 10, rectX, rectWidth, rectHeight, pageWidth, totalGeneral, seccionesTotales);
                        }
                    });
                } else {
                    continuarPDF(startY, rectX, rectWidth, rectHeight, pageWidth, totalGeneral, seccionesTotales);
                }
            };

            function continuarPDF(startY: number, rectX: number, rectWidth: number, rectHeight: number, pageWidth: number, totalGeneral: number, seccionesTotales: Map<string, number>) {
                const pageHeight = doc.internal.pageSize.getHeight();
                // -------------------------------
                // TÍTULO DE LA EVALUACIÓN
                // -------------------------------
                // if (startY + 60 > pageHeight) {
                //     doc.addPage();
                //     startY = 20;
                // }
                doc.addPage();
                startY = 20;

                // Texto azul centrado
                doc.setFontSize(12);
                doc.setFont('helvetica', 'bold');
                doc.setTextColor(primary);

                const text = evaluacion.nombre.toUpperCase() ?? '';
                const maxTextWidth = rectWidth - 10; // Margen horizontal dentro del rectángulo

                // Divide el texto en líneas que quepan
                const lineHeight = 6; // Alto de línea
                const lines = doc.splitTextToSize(text, maxTextWidth);
                const totalTextHeight = lines.length * lineHeight;

                // Ajusta el alto del rectángulo dinámicamente (mínimo 10 si no hay tanto texto)
                const dynamicRectHeight = Math.max(totalTextHeight + 5, 10); // +5 por padding

                // Fondo gris
                doc.setFillColor(accent300);
                doc.setDrawColor(primary);
                doc.rect(rectX, startY, rectWidth, dynamicRectHeight, 'FD'); // Fill + Draw

                // Centrar el bloque de texto dentro del nuevo alto
                let textY = startY + (dynamicRectHeight - totalTextHeight) / 2 + lineHeight / 2;
                lines.forEach(line => {
                    const lineWidth = doc.getTextWidth(line);
                    const textX = rectX + (rectWidth - lineWidth) / 2;
                    doc.text(line, textX, textY);
                    textY += lineHeight;
                });

                startY += dynamicRectHeight + 5; // Siguiente sección debajo del rectángulo

                // -------------------------------
                // NUEVO FORMATO DE TABLA POR SECCIÓN
                // -------------------------------
                evaluacion.secciones.forEach((seccion, index) => {
                    const pageHeight = doc.internal.pageSize.getHeight();
                    if (startY + 60 > pageHeight) {
                        doc.addPage();
                        startY = 20;
                    }

                    // 🔹 Título de la sección
                    doc.setFillColor(primary);
                    doc.rect(rectX, startY, rectWidth, rectHeight, 'F');
                    doc.setDrawColor(0);
                    doc.rect(rectX, startY, rectWidth, rectHeight);
                    doc.setFontSize(12);
                    doc.setTextColor(255, 255, 255);
                    doc.setFont('helvetica', 'bold');
                    doc.text(`SECCIÓN ${index + 1}: ${seccion.nombre}`, rectX + 3, startY + rectHeight - 3);
                    startY += rectHeight + 5;

                    const body: any[] = [];
                    let totalRiesgoSeccion = 0;

                    if (seccion.tienePuntuacion) {
                        // 🔹 FORMATO TABLA CON PUNTUACIÓN (respuestas como columnas)
                        const resultHeaders = seccion.preguntas[0]?.respuestas || [];

                        const headerRow = [
                            { content: 'Pregunta' },
                            { content: 'Resultado', colSpan: resultHeaders.length, styles: { halign: 'center' as const } },
                            { content: 'Observaciones' }
                        ];

                        seccion.preguntas.forEach((pregunta, idx) => {
                            const respuestaSeleccionada = pregunta.respuestas.find(
                                (resp) => pregunta.contestaciones?.some(
                                    (cont) => cont.idRespuesta === resp.idRespuesta
                                )
                            );

                            const observacion = pregunta.contestaciones?.find(
                                (cont) => pregunta.respuestas?.some(
                                    (resp) => resp.idRespuesta === cont.idRespuesta
                                )
                            )?.observacion || '';


                            const row: any[] = [
                                `${idx + 1}. ${pregunta.texto}`
                            ];

                            pregunta.respuestas.forEach((respuesta) => {
                                const isSelected = respuestaSeleccionada?.idRespuesta === respuesta.idRespuesta;
                                row.push({
                                    content: extractBoldTextOrPlain(respuesta.respuesta),
                                    styles: {
                                        fillColor: isSelected ? [203, 213, 225] : undefined
                                    }
                                });
                            });

                            row.push(observacion);
                            body.push(row);
                        });

                        // -----------------------------
                        // Cálculo de totales por opción
                        // -----------------------------
                        const contadorOpciones: Record<string, number> = {};
                        let totalPuntuacion = 0;

                        resultHeaders.forEach((respuesta) => {
                            contadorOpciones[extractBoldTextOrPlain(respuesta.respuesta)] = 0;
                        });

                        seccion.preguntas.forEach((pregunta) => {
                            const contestacion = pregunta.contestaciones?.[0];
                            const respuestaSeleccionada = pregunta.respuestas.find(
                                (resp) => contestacion?.idRespuesta === resp.idRespuesta
                            );

                            if (respuestaSeleccionada) {
                                const key = extractBoldTextOrPlain(respuestaSeleccionada.respuesta);
                                contadorOpciones[key] = (contadorOpciones[key] || 0) + 1;
                                totalPuntuacion += respuestaSeleccionada.valorRespuesta || 0;
                            }
                        });

                        // 🧾 Fila de totales por opción
                        const totalRow: any[] = [
                            { content: 'TOTAL', styles: { fontStyle: 'bold', halign: 'left' } }
                        ];

                        // Agregamos columnas con el conteo de cada respuesta
                        resultHeaders.forEach((respuesta) => {
                            const cantidad = contadorOpciones[extractBoldTextOrPlain(respuesta.respuesta)] || 0;
                            totalRow.push({ content: `${cantidad}`, styles: { fontStyle: 'bold', halign: 'center' } });
                        });

                        // Columna de observaciones vacía (si hay)
                        totalRow.push({ content: '', styles: { fontStyle: 'bold' } });

                        // Columna de puntuación total
                        totalRow.push({ content: totalPuntuacion.toFixed(2), styles: { fontStyle: 'bold', halign: 'center' } });

                        body.push(totalRow);

                        const maxObservacionLength = Math.max(
                            ...body.map(row => typeof row[row.length - 1] === 'string' ? row[row.length - 1].length : 0)
                        );

                        let observacionWidth = 32; // mínimo pequeño
                        if (maxObservacionLength > 80) {
                            observacionWidth = 80;
                        } else if (maxObservacionLength > 40) {
                            observacionWidth = 50;
                        }

                        autoTable(doc, {
                            startY,
                            head: [headerRow],
                            body,
                            theme: 'grid',
                            styles: {
                                fontSize: 10,
                                cellPadding: 3,
                                valign: 'middle',
                                font: 'helvetica',
                                halign: 'center',
                                overflow: 'linebreak'
                            },
                            headStyles: {
                                fillColor: [0, 163, 226],
                                textColor: 255,
                                fontStyle: 'bold'
                            },
                            columnStyles: {
                                0: { cellWidth: 60 }, // Pregunta
                                [1 + resultHeaders.length]: { cellWidth: observacionWidth }, // Observaciones
                                [resultHeaders.length + 2]: { cellWidth: 30 } // Total puntuación
                            },
                            didDrawPage: (data) => {
                                startY = data.cursor.y + 10;
                            }
                        });


                    } else {
                        // 🔹 FORMATO TABLA SIN PUNTUACIÓN (pregunta y respuestas en filas)
                        seccion.preguntas.forEach((pregunta, idx) => {
                            const contestacionLibre = pregunta.contestaciones?.[0]?.contestacion || '';
                            const observacion = pregunta.contestaciones?.[0]?.observacion || '';

                            // Fila principal con la pregunta
                            body.push([
                                { content: `${idx + 1}. ${pregunta.texto}`, colSpan: 2, styles: { halign: 'left', fontStyle: 'bold' } },
                            ]);

                            if (pregunta.respuestas.length > 0) {
                                // Tiene opciones predefinidas (puede seleccionar una o varias)
                                const respuestasSeleccionadasIds = pregunta.contestaciones?.map(cont => cont.idRespuesta) || [];

                                pregunta.respuestas.forEach((respuesta, i) => {
                                    const isSelected = respuestasSeleccionadasIds.includes(respuesta.idRespuesta);
                                    body.push([
                                        {
                                            content: `${String.fromCharCode(97 + i)}) ${extractBoldTextOrPlain(respuesta.respuesta)}`,
                                            colSpan: 3,
                                            styles: {
                                                halign: 'left',
                                                fillColor: isSelected ? [203, 213, 225] : undefined
                                            }
                                        },
                                        {
                                            content: isSelected ? '✔️' : '',
                                            styles: { halign: 'center' }
                                        }
                                    ]);
                                });
                            } else {
                                // Es una respuesta de texto libre
                                let respuestaTexto = contestacionLibre || '';

                                if (pregunta.categoria === 'PREG_FECHA' && contestacionLibre) {
                                    try {
                                        const fecha = new Date(contestacionLibre);
                                        if (!isNaN(fecha.getTime())) {
                                            // Formato dd/MM/yyyy
                                            const dia = String(fecha.getDate()).padStart(2, '0');
                                            const mes = String(fecha.getMonth() + 1).padStart(2, '0');
                                            const anio = fecha.getFullYear();
                                            respuestaTexto = `${dia}/${mes}/${anio}`;
                                        }
                                    } catch (e) {
                                        // Si no es una fecha válida, se deja el texto original
                                    }
                                }

                                body.push([
                                    { content: `${respuestaTexto}`, colSpan: 4, styles: { halign: 'left' } }
                                ]);
                            }

                            if (pregunta.contestaciones?.[0]?.observacion)
                                body.push([
                                    {
                                        content: `Observación: ${observacion}`,
                                        colSpan: 4,
                                        styles: { halign: 'left', fontStyle: 'italic' }
                                    }
                                ]);
                        });

                        autoTable(doc, {
                            startY,
                            body,
                            theme: 'grid',
                            margin: { right: 10 },
                            styles: {
                                fontSize: 10,
                                cellPadding: 3,
                                valign: 'middle',
                                font: 'helvetica',
                                halign: 'center'
                            },
                            // columnStyles: {
                            //     0: { cellWidth: 120 },
                            //     1: { cellWidth: 120 },
                            //     2: { cellWidth: 30 },
                            //     3: { cellWidth: 30 }
                            // },
                            didDrawPage: (data) => {
                                startY = data.cursor.y + 10;
                            }
                        });
                    }

                    startY += 15;
                });


                // -------------------------------
                // COMPROBAR SALTO DE PÁGINA ANTES DE LA TABLA DE RESUMEN
                // -------------------------------
                if (startY + 100 > pageHeight) {
                    doc.addPage();
                    startY = 20;
                }

                // -------------------------------
                // TABLA DE RESUMEN POR SECCIÓN
                // -------------------------------

                // Preparar datos
                const seccionesConPuntuacion = evaluacion.secciones.filter(seccion => seccion.tienePuntuacion);
                const opcionesSet = new Set<string>();
                const valorRespuestas: Record<string, number> = {};
                const totalGenerales: Record<string, number> = { totalRespuestas: 0 };

                //Solo se agrega la seccion si hay por lo menos 1 seccion con puntuacion
                if (seccionesConPuntuacion.length > 0) {

                    // -------------------------------
                    // TÍTULO "CALIFICACIÓN GENERAL"
                    // -------------------------------
                    const calificacionWidth = pageWidth - 40;
                    const calificacionHeight = 10;

                    doc.setFillColor(accent300); // Amarillo
                    doc.setDrawColor(primary); // Azul oscuro
                    doc.rect(rectX, startY, calificacionWidth, calificacionHeight, 'FD');

                    doc.setFontSize(12);
                    doc.setFont('helvetica', 'bold');
                    doc.setTextColor(primary); // Azul oscuro

                    const calificacionText = 'CALIFICACIÓN GENERAL';
                    const calificacionTextWidth = doc.getTextWidth(calificacionText);
                    const calificacionTextX = rectX + (calificacionWidth - calificacionTextWidth) / 2;
                    const calificacionTextY = startY + calificacionHeight / 2 + 3;
                    doc.text(calificacionText, calificacionTextX, calificacionTextY);

                    startY += calificacionHeight + 5;

                    seccionesConPuntuacion.forEach((seccion) => {
                        seccion.preguntas.forEach((pregunta) => {
                            pregunta.respuestas?.forEach((opcion) => {
                                opcionesSet.add(extractBoldTextOrPlain(opcion.respuesta));
                                valorRespuestas[extractBoldTextOrPlain(opcion.respuesta)] = opcion.valorRespuesta;
                            });
                        });
                    });

                    const opcionesUnicas = Array.from(opcionesSet);

                    // Columnas: Sección | [Opciones] | Total | Puntuación
                    const resumenHead = [
                        'Sección',
                        ...opcionesUnicas.map((op) => (op)),
                        'Total respuestas'
                    ];

                    // Cuerpo de la tabla
                    const resumenBody: any[] = [];

                    resumenBody.push(resumenHead);

                    seccionesConPuntuacion.forEach((seccion) => {
                        const fila: any[] = [{ content: seccion.nombre }];
                        const contador: Record<string, number> = {};
                        let totalRespuestas = 0;

                        opcionesUnicas.forEach((op) => contador[op] = 0);

                        seccion.preguntas.forEach((pregunta) => {
                            pregunta.contestaciones?.forEach((cont) => {
                                const seleccionada = pregunta.respuestas.find(r => r.idRespuesta === cont.idRespuesta);
                                if (seleccionada) {
                                    contador[extractBoldTextOrPlain(seleccionada.respuesta)]++;
                                    totalGenerales[extractBoldTextOrPlain(seleccionada.respuesta)] = (totalGenerales[extractBoldTextOrPlain(seleccionada.respuesta)] || 0) + 1;
                                    totalGenerales.puntuacion += seleccionada.valorRespuesta || 0;
                                    totalRespuestas++;
                                    totalGenerales.totalRespuestas++;
                                }
                            });
                        });

                        opcionesUnicas.forEach(op => fila.push({ content: contador[op].toString() }));
                        fila.push({ content: totalRespuestas.toString() });
                        resumenBody.push(fila);
                    });

                    // Fila de totales
                    const filaTotales: any[] = [{ content: 'TOTAL' }];
                    opcionesUnicas.forEach((op) => {
                        filaTotales.push({ content: (totalGenerales[op] || 0).toString() });
                    });
                    filaTotales.push({ content: totalGenerales.totalRespuestas.toString() });

                    resumenBody.push(filaTotales);

                    const MAX_COLUMNS = 6;
                    const bloques = [];

                    for (let i = 0; i < opcionesUnicas.length; i += MAX_COLUMNS) {
                        bloques.push(opcionesUnicas.slice(i, i + MAX_COLUMNS));
                    }

                    bloques.forEach((bloque, bloqueIndex) => {
                        const resumenHead = ['Sección', ...bloque, 'Total respuestas'];
                        const resumenBody: any[] = [resumenHead];

                        // Totales parciales por bloque
                        const totalPorBloque: Record<string, number> = {};
                        let totalRespuestasGenerales = 0;

                        bloque.forEach(op => totalPorBloque[op] = 0);

                        seccionesConPuntuacion.forEach((seccion) => {
                            const fila: any[] = [{ content: seccion.nombre }];
                            let totalRespuestas = 0;
                            const contador: Record<string, number> = {};
                            bloque.forEach((op) => contador[op] = 0);

                            seccion.preguntas.forEach((pregunta) => {
                                pregunta.contestaciones?.forEach((cont) => {
                                    const seleccionada = pregunta.respuestas.find(r => r.idRespuesta === cont.idRespuesta);
                                    if (seleccionada && bloque.includes(extractBoldTextOrPlain(seleccionada.respuesta))) {
                                        contador[extractBoldTextOrPlain(seleccionada.respuesta)]++;
                                        totalPorBloque[extractBoldTextOrPlain(seleccionada.respuesta)]++;
                                        totalRespuestas++;
                                        totalRespuestasGenerales++;
                                    }
                                });
                            });

                            bloque.forEach(op => fila.push({ content: contador[op].toString() }));
                            fila.push({ content: totalRespuestas.toString() });
                            resumenBody.push(fila);
                        });

                        // Fila de totales por bloque
                        const filaTotales: any[] = [{ content: 'TOTAL' }];
                        bloque.forEach(op => {
                            filaTotales.push({ content: totalPorBloque[op].toString() });
                        });
                        filaTotales.push({ content: totalRespuestasGenerales.toString() });
                        resumenBody.push(filaTotales);

                        // Generar tabla
                        autoTable(doc, {
                            body: resumenBody,
                            startY,
                            theme: 'grid',
                            styles: {
                                fontSize: 10,
                                cellPadding: 3,
                                valign: 'middle',
                                halign: 'center',
                                font: 'helvetica',
                                lineColor: [0, 0, 0],
                                lineWidth: 0.1
                            },
                            headStyles: {
                                fillColor: [203, 213, 225],
                                textColor: 0,
                                fontStyle: 'bold',
                                lineColor: [0, 0, 0],
                                lineWidth: 0.1
                            },
                            bodyStyles: {
                                fillColor: [255, 255, 255],
                                textColor: 0,
                                lineColor: [0, 0, 0],
                                lineWidth: 0.1
                            },
                            columnStyles: {
                                0: { fontStyle: 'bold' },
                                3: { fontStyle: 'bold' }
                            },
                            didParseCell: (data) => {
                                // Resaltar encabezado en azul
                                if (data.row.index === 0) {
                                    data.cell.styles.fillColor = [203, 213, 225];
                                    data.cell.styles.textColor = 0;
                                    data.cell.styles.fontStyle = 'bold';
                                }
                                // Negrita para la fila de total
                                if (data.row.index === resumenBody.length - 1) {
                                    data.cell.styles.fontStyle = 'bold';
                                }
                            }
                        });

                        startY = (doc as any).lastAutoTable.finalY + 10;
                    });

                    // -------------------------------
                    // INSERTAR EL GRÁFICO CIRCULAR
                    // -------------------------------
                    // Generar el gráfico y añadirlo al PDF

                    const totalesPorRespuesta: Record<string, number> = {};
                    opcionesUnicas.forEach(op => {
                        totalesPorRespuesta[op] = totalGenerales[op] || 0;
                    });

                    self.generarGraficoResumen(totalesPorRespuesta, (imgData) => {
                        if (imgData) {
                            // Añadir gráfico en el PDF
                            if (startY + 140 > pageHeight) {
                                doc.addPage();
                                startY = 20;
                            }
                            const imageWidth = 100;
                            const pageWidth = doc.internal.pageSize.getWidth();
                            const centerX = (pageWidth - imageWidth) / 2;

                            doc.addImage(imgData, 'PNG', 10, startY, 200, 100);
                            startY += 100;
                        }
                    });
                }

                // Convertir el PDF a Base64
                const pdfBase64 = doc.output('datauristring'); // Devuelve el PDF en formato Base64 con prefijo MIME
                base64Content = pdfBase64.split(',')[1]; // Eliminar el prefijo MIME para obtener solo el contenido Base64
                // console.log('PDF en Base64:');
                // console.log(base64Content);

                // Guardar el PDF
                // doc.save('Evaluacion.pdf');

                if (abrirEnNavegador) {
                    // Abrir el PDF en una nueva pestaña
                    const pdfBlob = doc.output('blob');
                    const pdfUrl = URL.createObjectURL(pdfBlob);
                    window.open(pdfUrl, '_blank');
                }

                // -------------------------------
                // Guardar el Base64 en un archivo .txt
                // -------------------------------
                // const blob = new Blob([base64Content], { type: 'text/plain' });
                // const link = document.createElement('a');
                // link.href = window.URL.createObjectURL(blob);
                // link.download = 'evaluacion_base64.txt';
                // link.click();
                resolve(base64Content);
            }

        });


    }

    /**
     * Informe SAVRY tipo siserv: secciones I–V (datos generales, resumen, detalle, valoración, justificación).
     */
    generarPdfSavry(encabezado: EncabezadoDTO, evaluacion: EncuestaDTO, abrirEnNavegador: boolean = true): Promise<string> {
        return new Promise((resolve) => {
            const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
            const pageWidth = doc.internal.pageSize.getWidth();
            const marginX = 14;
            const contentWidth = pageWidth - marginX * 2;
            let y = 16;

            const tituloSeccion = (texto: string) => {
                if (y > 270) {
                    doc.addPage();
                    y = 16;
                }
                doc.setFont('helvetica', 'bold');
                doc.setFontSize(12);
                doc.setTextColor(0, 0, 0);
                doc.text(texto, marginX, y);
                y += 6;
            };

            const txt = (valor: any): string => {
                if (valor === null || valor === undefined || valor === '') {
                    return '—';
                }
                return String(valor);
            };

            const fechaSolo = (f: any): string => {
                if (!f) return '—';
                try {
                    return this.funcionesUtils.formatearFechaSinHora(f) || '—';
                } catch {
                    return '—';
                }
            };

            // Título
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(14);
            doc.text('INFORME DE VALORACIÓN DE RIESGO', pageWidth / 2, y, { align: 'center' });
            y += 10;

            // I. Datos generales
            tituloSeccion('I. DATOS GENERALES');

            const adolescente = (evaluacion.adolescente || '').toUpperCase();
            const establecimiento =
                evaluacion.establecimiento ||
                evaluacion.tipoCentro ||
                '';
            const evaluador =
                evaluacion.evaluador ||
                encabezado?.nombreUsuarioCrea ||
                encabezado?.nombreUsuarioEdita ||
                '';
            const fechaRegistro = evaluacion.fechaRegistro || encabezado?.fechaCreacion;
            const fechaEvaluacion =
                evaluacion.fechaEvaluacion ||
                encabezado?.fechaCompletacion ||
                evaluacion.fechaValoracion;
            const edadMostrar = this.resolverEdadAdolescente(
                evaluacion.edadAdolescente,
                evaluacion.fechaNacimientoAdolescente
            );

            autoTable(doc, {
                startY: y,
                margin: { left: marginX, right: marginX },
                theme: 'grid',
                styles: { fontSize: 8, cellPadding: 2, valign: 'middle' },
                columnStyles: {
                    0: { fontStyle: 'bold', cellWidth: 42, fillColor: [240, 240, 240] },
                    1: { cellWidth: 55 },
                    2: { fontStyle: 'bold', cellWidth: 38, fillColor: [240, 240, 240] },
                    3: { cellWidth: contentWidth - 42 - 55 - 38 },
                },
                body: [
                    [
                        'Adolescente evaluado',
                        adolescente,
                        'Edad',
                        edadMostrar,
                    ],
                    [
                        'Fecha de Nacimiento',
                        fechaSolo(evaluacion.fechaNacimientoAdolescente),
                        'Correlativo',
                        txt(evaluacion.correlativo),
                    ],
                    [
                        'Establecimiento',
                        txt(establecimiento).toUpperCase(),
                        'Fecha de Registro',
                        fechaSolo(fechaRegistro),
                    ],
                    [
                        'Evaluador(a)',
                        txt(evaluador).toUpperCase(),
                        'Fecha de Evaluación',
                        fechaSolo(fechaEvaluacion),
                    ],
                ],
            });
            y = (doc as any).lastAutoTable.finalY + 8;

            // II. Resumen
            tituloSeccion('II. RESUMEN DE VALORACIÓN POR GRUPO');
            const resumen: SavryGrupoResumen[] = calcularResumenSavryDesdeEncuesta(evaluacion);
            autoTable(doc, {
                startY: y,
                margin: { left: marginX, right: marginX },
                theme: 'grid',
                head: [['Grupo', 'Bajo', 'Medio', 'Alto', 'Presente', 'Ausente', 'Criticos']],
                body: resumen.map((r) => [
                    r.grupo,
                    r.bajo,
                    r.medio,
                    r.alto,
                    r.presente,
                    r.ausente,
                    r.criticos,
                ]),
                styles: { fontSize: 8, cellPadding: 2, halign: 'center', valign: 'middle' },
                headStyles: {
                    fillColor: [60, 60, 60],
                    textColor: 255,
                    fontStyle: 'bold',
                    halign: 'center',
                },
                columnStyles: {
                    0: { halign: 'left', cellWidth: 70 },
                },
                didParseCell: (data) => {
                    if (data.section === 'body' && data.column.index >= 1) {
                        const colores: Record<number, [number, number, number]> = {
                            1: [220, 252, 231],
                            2: [254, 249, 195],
                            3: [254, 226, 226],
                            4: [219, 234, 254],
                            5: [241, 245, 249],
                            6: [255, 237, 213],
                        };
                        const c = colores[data.column.index];
                        if (c) {
                            data.cell.styles.fillColor = c;
                        }
                    }
                },
            });
            y = (doc as any).lastAutoTable.finalY + 8;

            // III. Detalle
            tituloSeccion('III. DETALLE DE RESPUESTAS POR GRUPO');
            const detalle: SavryDetalleItem[] = calcularDetalleSavryDesdeEncuesta(evaluacion);
            const gruposOrden = Array.from(new Set(detalle.map((d) => d.grupo)));

            gruposOrden.forEach((grupo) => {
                if (y > 250) {
                    doc.addPage();
                    y = 16;
                }
                doc.setFont('helvetica', 'bold');
                doc.setFontSize(9);
                doc.setTextColor(0, 0, 0);
                const grupoLines = doc.splitTextToSize(grupo, contentWidth);
                doc.text(grupoLines, marginX, y);
                y += grupoLines.length * 4 + 2;

                const filasGrupo = detalle.filter((d) => d.grupo === grupo);
                autoTable(doc, {
                    startY: y,
                    margin: { left: marginX, right: marginX },
                    theme: 'grid',
                    head: [['#', 'Pregunta', 'Valor', 'Critico']],
                    body: filasGrupo.map((d) => [
                        d.numero,
                        d.pregunta,
                        d.valor,
                        d.critico ? 'SI' : 'NO',
                    ]),
                    styles: { fontSize: 7.5, cellPadding: 1.5, valign: 'middle' },
                    headStyles: {
                        fillColor: [80, 80, 80],
                        textColor: 255,
                        fontStyle: 'bold',
                        halign: 'center',
                    },
                    columnStyles: {
                        0: { cellWidth: 10, halign: 'center' },
                        1: { cellWidth: contentWidth - 10 - 28 - 18 },
                        2: { cellWidth: 28, halign: 'center', fontStyle: 'bold' },
                        3: { cellWidth: 18, halign: 'center' },
                    },
                });
                y = (doc as any).lastAutoTable.finalY + 6;
            });

            // IV. Valoración final (espacio extra respecto al último grupo, p.ej. factores de protección)
            y += 12;
            if (y > 245) {
                doc.addPage();
                y = 16;
            }
            tituloSeccion('IV. VALORACIÓN FINAL');
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(11);
            const nivel = (evaluacion.nombreValoracionFinal || '—').toUpperCase();
            doc.text(`NIVEL DE RIESGO: ${nivel}`, marginX, y);
            y += 10;

            // V. Justificación
            tituloSeccion('V. JUSTIFICACIÓN DEL EVALUADOR');
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(10);
            const justif = evaluacion.justificacionValoracion || '—';
            const justifLines = doc.splitTextToSize(justif, contentWidth);
            justifLines.forEach((line: string) => {
                if (y > 280) {
                    doc.addPage();
                    y = 16;
                }
                doc.text(line, marginX, y);
                y += 5;
            });

            const pdfBase64 = doc.output('datauristring');
            const base64Content = pdfBase64.includes(',') ? pdfBase64.split(',')[1] : pdfBase64;

            if (abrirEnNavegador) {
                const pdfBlob = doc.output('blob');
                const pdfUrl = URL.createObjectURL(pdfBlob);
                window.open(pdfUrl, '_blank');
            }

            resolve(base64Content);
        });
    }

    /** Edad desde ficha o calculada con fecha de nacimiento. */
    private resolverEdadAdolescente(edad: number | null | undefined, fechaNacimiento: any): string {
        if (edad !== null && edad !== undefined && !Number.isNaN(Number(edad)) && Number(edad) > 0) {
            return String(edad);
        }
        if (!fechaNacimiento) {
            return '—';
        }
        const nac = new Date(fechaNacimiento);
        if (isNaN(nac.getTime())) {
            return '—';
        }
        const hoy = new Date();
        let years = hoy.getFullYear() - nac.getFullYear();
        const m = hoy.getMonth() - nac.getMonth();
        if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) {
            years -= 1;
        }
        return years >= 0 ? String(years) : '—';
    }

    generarReporteConEncabezado(encabezadoDTO: EncabezadoDTO) {
        console.log("Ver encabezado seleccionado: ", encabezadoDTO);

        let load = this.dialogMensajeService.mensajeLoading("Generando reporte...");
        this.encuestaService.obtenerEvaluacionPorTokenEncabezado(encabezadoDTO, this.nemonicoMenu).subscribe(
            {
                next: (response: RespuestaPorDefecto<EncuestaDTO>) => {
                    load.close();
                    if (!response.exito) {
                        this.dialogMensajeService.mensajeError(
                            'Hubo un problema al recuperar los registros.' + response.mensaje
                        );
                        return;
                    }
                    console.log("Encuesta relacionada: ", response.data);
                    this.generarPDF(encabezadoDTO, response.data);
                },
                error: (error: any) => {
                    this.dialogMensajeService.mensajeError(
                        'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                    );
                }
            }
        );
    }
}

function generarColores(cantidad: number): string[] {
    // Colores predefinidos de una lista específica
    const coloresPredefinidos = [
        'rgba(0, 163, 226, 0.8)',  // Azul
        'rgba(202, 210, 0, 0.8)',  // Amarillo
        'rgba(230, 41, 132, 0.8)',  // Rosado
        'rgba(181,212,69, 0.8)', // Verde
        'rgba(255, 159, 64, 0.8)',  // Naranja
        'rgba(72, 75, 84, 0.8)'  // Gris
    ];

    const colores = [];

    // Primero intentamos agregar colores predefinidos
    for (let i = 0; i < Math.min(cantidad, coloresPredefinidos.length); i++) {
        colores.push(coloresPredefinidos[i]);
    }

    // Si hay más colores requeridos, generamos aleatoriamente el resto
    for (let i = colores.length; i < cantidad; i++) {
        const r = Math.floor(Math.random() * 200);
        const g = Math.floor(Math.random() * 200);
        const b = Math.floor(Math.random() * 200);
        colores.push(`rgba(${r}, ${g}, ${b}, 0.8)`);
    }

    return colores;
}


function rgbaStringToRgbArray(rgbaStr: string): [number, number, number] {
    const match = rgbaStr.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
    if (match) {
        return [parseInt(match[1]), parseInt(match[2]), parseInt(match[3])] as [number, number, number];
    }
    return [255, 255, 255]; // Color blanco por defecto si no hay match
}

function extractBoldTextOrPlain(html) {
    const matches = html.match(/<b>(.*?)<\/b>/gi);
    if (matches) {
        return matches.map(m => m.replace(/<\/?b>/gi, "")).join(" ");
    } else {
        const div = document.createElement("div");
        div.innerHTML = html;
        return div.textContent || div.innerText || "";
    }
}