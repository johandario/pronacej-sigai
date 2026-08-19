import moment from "moment";
import { ObjectoArbol } from "../components/seleccionar-objecto-del-arbol/ObjectoArbol.model";
import { CatalogoDTO } from "../model/both/catalogoDTO.model";
import { Injectable } from "@angular/core";
import { RespuestaPorDefecto } from "../model/response/RespuestaPorDefecto.model";
import { CatalogoService } from "../services/catalogo.service";
import { catchError, firstValueFrom, map, Observable, throwError } from "rxjs";
import { MatDatepickerInputEvent } from "@angular/material/datepicker";
import { JerarquiaDTO } from "../model/both/jerarquiaDTO.model";
import { JerarquiaService } from "app/modules/seguridad/services/jerarquia.service";
import { NativeDateAdapter } from "@angular/material/core";
import { HttpClient } from "@angular/common/http";
import { Buffer } from "buffer";
import { FormGroup } from "@angular/forms";

// Constantes para formatos de fecha
export const CUSTOM_DATE_FORMATS = {
  parse: {
    dateInput: 'DD/MM/YYYY'
  },
  display: {
    dateInput: 'DD/MM/YYYY',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY'
  }
};

// Clase adaptadora para formateo de fechas
export class CustomDateAdapter extends NativeDateAdapter {
  override format(date: Date, displayFormat: Object): string {
    if (displayFormat === 'DD/MM/YYYY') {
      const day = date.getDate();
      const month = date.getMonth() + 1;
      const year = date.getFullYear();
      return `${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${year}`;
    }
    return date.toDateString();
  }

  override parse(value: any): Date | null {
    if (typeof value === 'string') {
      const parts = value.split('/');
      if (parts.length === 3) {
        const day = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10) - 1;
        const year = parseInt(parts[2], 10);
        if (!isNaN(day) && !isNaN(month) && !isNaN(year)) {
          const date = new Date(year, month, day);
          if (date.getFullYear() === year && date.getMonth() === month && date.getDate() === day) {
            return date;
          }
        }
      }
    }
    return super.parse(value);
  }
}

@Injectable({
  providedIn: 'root' // Esto hace que sea un singleton accesible globalmente
})
export class FuncionesUtils {

  constructor(
    private catalogoService: CatalogoService,
    private jerarquiaService: JerarquiaService,
    private http: HttpClient
  ) { }

  /**
   * Crea una estructura de árbol con los datos proporcionados
   */
  crearObjectoArbolConData<T>(objetoArbolList: T[], keyId: string,
    keyNombre: string, keyHijos: string, keyIcon?: string): ObjectoArbol<T>[] {
    let objectosArbol: ObjectoArbol<T>[] = [];
    for (let i = 0; objetoArbolList.length > i; i++) {
      let objetoArbol = objetoArbolList[i];
      objectosArbol.push(
        this.crearObjetoArbol<T>(objetoArbol, keyId, keyNombre, keyHijos,
          keyIcon
        )
      );
    }

    return objectosArbol;
  }

  /**
   * Crea un objeto árbol individual con sus propiedades
   */
  private crearObjetoArbol<T>(obtjetoArbol: T, keyId: string, keyNombre: string, keyHijos: string,
    keyIcon?: string): ObjectoArbol<T> {
    if (!obtjetoArbol) {
      return null;
    }

    let objectoArbol = new ObjectoArbol<T>();
    objectoArbol.data = obtjetoArbol;
    objectoArbol.id = obtjetoArbol[keyId];
    objectoArbol.nombre = obtjetoArbol[keyNombre];

    if (keyIcon) {
      objectoArbol.icono = obtjetoArbol[keyIcon];
    }

    if (!obtjetoArbol[keyHijos] || obtjetoArbol[keyHijos]?.length == 0) {
      return objectoArbol;
    }

    objectoArbol.hijos = [];

    for (let i = 0; obtjetoArbol[keyHijos].length > i; i++) {
      let objetoHijo = obtjetoArbol[keyHijos][i];
      objectoArbol.hijos.push(
        this.crearObjetoArbol(objetoHijo,
          keyId, keyNombre, keyHijos, keyIcon)
      );
    }

    return objectoArbol;
  }

  /**
   * Convierte un archivo a formato Base64
   */
  obtenerBase64(file: File) {
    let promise = new Promise<string | ArrayBuffer>(
      (resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
      }
    );

    return promise;
  }

  /**
   * Formatea una fecha con hora local en formato DD-MM-YYYY HH:mm:ss
   */
  getLocalDate(date: Date) {
    // return moment(date, "YYYY-MM-DDTHH:mm:ssZ").toDate().toLocaleString();
    return moment(date).format("DD-MM-YYYY HH:mm:ss");
  }

  /**
   * Formatea una fecha en formato DD/MM/YYYY
   */
  getOnlyDate(date: Date) {
    return moment(date).format("DD/MM/YYYY");
  }

  /**
   * Formatea una fecha en formato DD/MM/YYYY
   */
  formatearFecha(fecha: string | Date): string {
    if (!fecha) return '';

    const fechaObj = new Date(fecha);
    const dia = fechaObj.getDate().toString().padStart(2, '0');
    const mes = (fechaObj.getMonth() + 1).toString().padStart(2, '0');
    const anio = fechaObj.getFullYear();

    return `${dia}/${mes}/${anio}`;
  }

  /**
   * Formatea solo la hora de una fecha en formato local español
   */
  formatearHora(fecha: string | Date): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }

  /**
   * Formatea fecha y hora en formato DD/MM/YYYY HH:mm:ss
   */
  formatearFechaHora(fecha: string | Date): string {
    if (!fecha) return '';

    const fechaObj = new Date(fecha);
    const dia = fechaObj.getDate().toString().padStart(2, '0');
    const mes = (fechaObj.getMonth() + 1).toString().padStart(2, '0');
    const anio = fechaObj.getFullYear();

    const fechaFormateada = `${dia}/${mes}/${anio}`;



    const date = new Date(fecha);
    const horaFormateada = date.toLocaleTimeString('es-ES');

    return `${fechaFormateada} ${horaFormateada}`
  }

  /**
   * Convierte bytes a formato legible (KB, MB, GB, etc.)
   */
  formatBytes(bytes: number, decimals = 2) {
    if (!+bytes) return '0 Bytes'

    const k = 1024
    const dm = decimals < 0 ? 0 : decimals
    const sizes = ['Bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB']

    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`
  }

  /**
   * Convierte tiempo HH:MM a decimal de horas
   */
  convertirTiempoADecimal(tiempo: string): number {
    const [horas, minutos] = tiempo.split(':').map(Number);
    return horas + minutos / 60;
  }

  /**
   * Convierte decimal de horas a formato HH:MM
   * @param decimalHoras Número decimal que representa horas y minutos
   * @returns Cadena en formato HH:MM
   */
  convertirDecimalATiempo(decimalHoras: number | null | undefined): string {
    // Manejar valores nulos o indefinidos
    if (decimalHoras === null || decimalHoras === undefined || isNaN(decimalHoras)) {
      return '';
    }

    const horas = Math.floor(decimalHoras);  // Parte entera: horas
    const minutos = Math.round((decimalHoras - horas) * 60);  // Parte decimal convertida a minutos

    // Formatea para asegurarse de que tenga dos dígitos
    const horasStr = horas.toString().padStart(2, '0');
    const minutosStr = minutos.toString().padStart(2, '0');

    return `${horasStr}:${minutosStr}`;
  }

  /**
   * Convierte ArrayBuffer a string Base64
   */
  arrayBufferToBase64(buffer: ArrayBuffer) {
    var binary = '';
    var bytes = new Uint8Array(buffer);
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }

  /**
   * Obtiene el nombre de un catálogo por su token identificador
   */
  obtenerNombreCatalogoPorToken(tokenIdentificador: string, listaCatalogo: CatalogoDTO[]): string {
    const artefacto = listaCatalogo.find(
      item => item.tokenIdentificador === tokenIdentificador
    );
    return artefacto ? artefacto.nombre : 'Nombre no disponible';
  }

  /**
   * Obtiene lista de catálogos hijos de un nemónico padre
   */
  obtenerListaCatalogo(nemonicoPadre: string, nemonicoMenu: string): Observable<CatalogoDTO[]> {
    return this.catalogoService.obtenerHijos(nemonicoPadre, nemonicoMenu).pipe(
      map((response: RespuestaPorDefecto<CatalogoDTO[]>) => {
        if (!response.exito) {
          throw new Error(response.mensaje);
        }
        return response.data;
      }),
      catchError((error) => {
        console.error('Error obteniendo lista de catálogo:', error);
        return throwError(() => new Error('Error al obtener la lista de catálogo.'));
      }),
    );
  }

  /**
   * Obtiene lista de jerarquías por nemónico padre
   */
  obtenerListaJerarquia(nemonicoPadre: string, nemonicoMenu: string): Observable<JerarquiaDTO[]> {
    return this.jerarquiaService.obtenerJerarquiasPorNemonicoPadre(nemonicoPadre, nemonicoMenu).pipe(
      map((response: RespuestaPorDefecto<JerarquiaDTO[]>) => {
        if (!response.exito) {
          throw new Error(response.mensaje);
        }
        return response.data;
      }),
      catchError((error) => {
        console.error('Error obteniendo lista de jerarquías:', error);
        return throwError(() => new Error('Error al obtener la lista de jerarquías.'));
      }),
    );
  }

  /**
   * Calcula la edad en años basándose en fecha de nacimiento
   */
  getEdad(fechaNacimientoString: string): number {
    const fechaNacimiento = new Date(fechaNacimientoString);
    const hoy = new Date();

    let edad = hoy.getFullYear() - fechaNacimiento.getFullYear();
    const mesActual = hoy.getMonth();
    const diaActual = hoy.getDate();

    const mesNacimiento = fechaNacimiento.getMonth();
    const diaNacimiento = fechaNacimiento.getDate();

    // Restar un año si aún no se ha cumplido el cumpleaños este año
    if (mesActual < mesNacimiento || (mesActual === mesNacimiento && diaActual < diaNacimiento)) {
      edad--;
    }

    return edad;
  }

  /**
   * Convierte string Base64 a Blob para archivos PDF
   */
  getPdfBlob(base64: string): Blob {
    // Decodificar el Base64 en un Blob
    const byteCharacters = atob(base64); // Decodifica el Base64
    const byteNumbers = new Array(byteCharacters.length).fill(0).map((_, i) => byteCharacters.charCodeAt(i));
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: 'application/pdf' });

    return blob;
  }

  /**
   * Calcula edad en años a partir de una fecha específica
   */
  calcularEdadEnYears(date: Date) {
    const fechaNacimiento = new Date(date);
    const ahora = new Date();
    let edad = ahora.getFullYear() - fechaNacimiento.getFullYear();

    // Verificar si el cumpleaños de este año ya ha pasado
    const mesActual = ahora.getMonth() + 1;
    const diaActual = ahora.getDate();
    const mesNacimiento = fechaNacimiento.getMonth() + 1;
    const diaNacimiento = fechaNacimiento.getDate();

    if (mesActual < mesNacimiento || (mesActual === mesNacimiento && diaActual < diaNacimiento)) {
      edad--;
    }

    return edad;
  }

  /**
   * Calcula edad desde un evento de datepicker
   */
  calcularEdad(event: MatDatepickerInputEvent<Date>) {
    const fechaSeleccionada = event.value; // Obtiene la fecha seleccionada
    if (fechaSeleccionada) {
      const hoy = new Date();
      const edad = hoy.getFullYear() - fechaSeleccionada.getFullYear();
      const mes = hoy.getMonth() - fechaSeleccionada.getMonth();
      const dia = hoy.getDate() - fechaSeleccionada.getDate();

      // Ajustar edad si el mes o día actual es menor que el de nacimiento
      const edadFinal = mes < 0 || (mes === 0 && dia < 0) ? edad - 1 : edad;
      return edadFinal
    }
    return 0;
  }

  /**
   * Convierte número a numeración romana (1-20)
   */
  convertirARomano(num: number): string {
    const romanNumerals: { [key: number]: string } = {
      1: "I",
      2: "II",
      3: "III",
      4: "IV",
      5: "V",
      6: "VI",
      7: "VII",
      8: "VIII",
      9: "IX",
      10: "X",
      11: "XI",
      12: "XII",
      13: "XIII",
      14: "XIV",
      15: "XV",
      16: "XVI",
      17: "XVII",
      18: "XVIII",
      19: "XIX",
      20: "XX",
    };

    return romanNumerals[num] || num.toString(); // Si el número supera los definidos, usa el número como texto.
  }

  /**
   * Compara dos objetos CatalogoDTO para select/mat-select
   */
  compararCatalogosSelect(catalogo1: CatalogoDTO, catalogo2: CatalogoDTO): boolean {
    return catalogo1 && catalogo2 ? catalogo1.nemonico === catalogo2.nemonico : catalogo1 === catalogo2;
  }

  /**
   * Compara dos objetos JerarquiaDTO para select/mat-select
   */
  compararJerarquiaSelect(jerarquia1: JerarquiaDTO, jerarquia2: JerarquiaDTO): boolean {
    return jerarquia1 && jerarquia2 ? jerarquia1.nombre === jerarquia2.nombre : jerarquia1 === jerarquia2;
  }

  /**
   * Obtiene nombre de localidad por token identificador
   */
  obtenerNombreLocalidadPorToken(tokenIdentificador: string, listaCatalogo: any[]): string {
    console.log('lista', listaCatalogo)
    const artefacto = listaCatalogo.find(
      item => item.nemonico === tokenIdentificador
    );
    return artefacto ? artefacto.nombre : 'Nombre no disponible';
  }

  /**
   * Obtiene nombre de catálogo por nemónico
   */
  obtenerNombreCatalogoPorNemonico(nemonico: string, listaCatalogo: CatalogoDTO[]): string {
    const artefacto = listaCatalogo.find(
      item => item.nemonico === nemonico
    );
    return artefacto ? artefacto.nombre : 'Nombre no disponible';
  }

  /**
   * Resetea las horas, minutos y segundos de una fecha a 00:00:00
   */
  resetearHoraFecha(fecha: Date): Date {
    const nuevaFecha = new Date(fecha);
    nuevaFecha.setHours(0, 0, 0, 0);
    return nuevaFecha;
  }

  /**
   * Aplica una hora específica a una fecha
   */
  aplicarHoraAFecha(fecha: Date, horaString: string): Date {
    const fechaResultado = new Date(fecha);
    const [horas, minutos] = horaString.split(':');
    fechaResultado.setHours(parseInt(horas), parseInt(minutos));
    return fechaResultado;
  }

  /**
   * Parsea una fecha en formato string dd/mm/yyyy a objeto Date
   */
  parseManualDate(dateStr: string): Date | null {
    if (!dateStr) return null;

    const parts = dateStr.split('/');
    if (parts.length !== 3) return null;

    const day = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1; // Months are 0-based in JS
    const year = parseInt(parts[2], 10);

    if (isNaN(day) || isNaN(month) || isNaN(year)) return null;

    const date = new Date(year, month, day);

    // Validate date (check if parsing created a valid date)
    if (date.getFullYear() !== year || date.getMonth() !== month || date.getDate() !== day) {
      return null;
    }

    return date;
  }

  /**
   * Convierte número de mes (0-11) a nombre en español
   */
  convertirNumeroMesATexto(num: number): string {
    const meses: { [key: number]: string } = {
      0: "Enero",
      1: "Febrero",
      2: "Marzo",
      3: "Abril",
      4: "Mayo",
      5: "Junio",
      6: "Julio",
      7: "Agosto",
      8: "Septiembre",
      9: "Octubre",
      10: "Noviembre",
      11: "Diciembre",
    };

    return meses[num] || num.toString(); // Si el número supera los definidos, usa el número como texto.
  }

  /**
   * Escapa caracteres HTML especiales para prevenir problemas en la generación de PDFs
   */
  escaparHTML(texto: string): string {
    if (!texto) return '';
    return texto
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  /**
   * Filtra un array de objetos por fecha según un texto de filtro
   */
  filtrarPorFecha<T>(items: T[], filtro: string, campoFecha: string): T[] {
    if (!filtro || filtro.trim() === '' || !campoFecha || !items || items.length === 0) {
      return items;
    }

    const filtroLimpio = filtro.toLowerCase().trim();

    return items.filter(item => {
      if (!item[campoFecha]) return false;

      // Convertir a fecha JavaScript
      const fecha = new Date(item[campoFecha]);

      // Verificar si es una fecha válida
      if (isNaN(fecha.getTime())) return false;

      // Crear un conjunto de representaciones de fecha para buscar
      const dia = fecha.getDate().toString().padStart(2, '0');
      const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
      const anio = fecha.getFullYear().toString();
      const horas = fecha.getHours().toString().padStart(2, '0');
      const minutos = fecha.getMinutes().toString().padStart(2, '0');
      const segundos = fecha.getSeconds().toString().padStart(2, '0');

      // Formatos de fecha para buscar
      const formatos = [
        `${dia}-${mes}-${anio}`,      // DD-MM-YYYY
        `${dia}/${mes}/${anio}`,      // DD/MM/YYYY
        `${dia}-${mes}-${anio} ${horas}:${minutos}:${segundos}`, // DD-MM-YYYY HH:MM:SS
        `${dia}/${mes}/${anio} ${horas}:${minutos}:${segundos}`, // DD/MM/YYYY HH:MM:SS
        dia,                          // DD
        mes,                          // MM
        anio,                         // YYYY
        `${horas}:${minutos}`,        // HH:MM
        `${horas}:${minutos}:${segundos}` // HH:MM:SS
      ];

      // También agregar el nombre del mes como texto para búsquedas como "enero"
      const nombreMes = this.convertirNumeroMesATexto(fecha.getMonth()).toLowerCase();
      formatos.push(nombreMes);

      // Verificar si alguno de los formatos contiene el filtro
      return formatos.some(formato => formato.includes(filtroLimpio));
    });
  }

  /**
   * Determina si un texto podría ser un filtro de fecha
   */
  esPosibleFiltroFecha(filtro: string): boolean {
    if (!filtro || filtro.trim() === '') return false;

    // Si contiene al menos un número, podría ser parte de una fecha
    if (/\d/.test(filtro)) return true;

    // Si contiene el nombre de un mes, también podría ser fecha
    const nombresMeses = [
      'enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
      'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'
    ];

    return nombresMeses.some(mes => filtro.toLowerCase().includes(mes));
  }

  /**
   * Filtra un array de objetos por un campo de duración según un texto de filtro
   */
  filtrarPorDuracion<T>(items: T[], filtro: string, campoDuracion: string, campoFormateado?: string): T[] {
    if (!items || items.length === 0 || !filtro || filtro.trim() === '') {
      return items;
    }

    const filtroLimpio = filtro.trim().toLowerCase();

    return items.filter(item => {
      // Obtener el valor de duración
      const duracionItem = item[campoDuracion as keyof T];
      if (!duracionItem && duracionItem !== 0) {
        return false;
      }

      // Comprobar si hay un valor formateado para buscar también en él
      if (campoFormateado) {
        const duracionFormateada = item[campoFormateado as keyof T];
        if (duracionFormateada && typeof duracionFormateada === 'string') {
          if (duracionFormateada.toLowerCase().includes(filtroLimpio)) {
            return true;
          }
        }
      }

      // Si es un número, convertirlo a string para buscar
      if (typeof duracionItem === 'number') {
        const duracionStr = duracionItem.toString();
        if (duracionStr.includes(filtroLimpio)) {
          return true;
        }

        // Intentar formatear como tiempo y verificar
        const horas = Math.floor(duracionItem);
        const minutos = Math.round((duracionItem - horas) * 60);
        const duracionFormatoHora = `${horas}:${minutos.toString().padStart(2, '0')}`;

        return duracionFormatoHora.includes(filtroLimpio);
      }

      return false;
    });
  }

  /**
   * Determina si un texto podría ser un filtro de duración o tiempo
   */
  esFiltroDeDuracion(filtro: string): boolean {
    if (!filtro || filtro.trim() === '') {
      return false;
    }

    // Eliminar espacios
    const limpio = filtro.trim();

    // Verificar si es un número entero
    if (/^\d+$/.test(limpio)) {
      return true;
    }

    // Verificar si es un número decimal
    if (/^\d+\.\d+$/.test(limpio)) {
      return true;
    }

    // Verificar si es un formato de tiempo (hh:mm)
    if (/^\d{1,2}:\d{2}$/.test(limpio)) {
      return true;
    }

    return false;
  }

  /**
   * Obtiene logo en formato Base64 para PDFs de forma asíncrona
   */
  async obtenerLogoPdf(): Promise<string> {
    const data = await firstValueFrom(
      this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
    );
    const base64String = this.arrayBufferToBase64(data);
    return `data:image/png;base64,${base64String}`;
  }

  // ==================== MÉTODOS PARA DETECCIÓN DE CAMBIOS ====================

  /**
   * Compara dos fechas para detectar cambios, maneja valores null/undefined
   */
  compararFechas(fecha1: Date | null | undefined, fecha2: Date | null | undefined): boolean {
    if (!fecha1 && !fecha2) return false;
    if (!fecha1 || !fecha2) return true;
    return new Date(fecha1).getTime() !== new Date(fecha2).getTime();
  }

  /**
   * Compara dos arrays de objetos para detectar cambios estructurales
   */
  compararArrays<T>(actual: T[], original: T[], compararFn: (a: T, b: T) => boolean): boolean {
    if (!actual && !original) return false;
    if (!actual || !original) return true;
    if (actual.length !== original.length) return true;

    for (let i = 0; i < actual.length; i++) {
      const itemActual = actual[i];
      const itemOriginal = original.find(orig => compararFn(itemActual, orig));

      if (!itemOriginal) {
        return true;
      }
    }

    return false;
  }

  /**
   * Detecta si hay cambios en un array comparando con el estado original
   */
  hayArrayModificado<T>(
    datosActuales: T[],
    datosOriginales: T[],
    campoId: keyof T,
    camposComparar: (keyof T)[]
  ): boolean {
    // Si las longitudes son diferentes, hay cambios
    if (datosActuales.length !== datosOriginales.length) {
      return true;
    }

    // Verificar si hay registros nuevos (campo ID === "0")
    const hayRegistrosNuevos = datosActuales.some(item =>
      item[campoId] === "0" || item[campoId] === 0
    );
    if (hayRegistrosNuevos) {
      return true;
    }

    // Verificar si hay cambios en registros existentes
    for (let i = 0; i < datosActuales.length; i++) {
      const actual = datosActuales[i];
      const original = datosOriginales.find(orig =>
        orig[campoId] === actual[campoId]
      );

      if (!original) {
        return true; // Registro no encontrado en original = nuevo
      }

      // Comparar campos especificados
      for (const campo of camposComparar) {
        const valorActual = actual[campo];
        const valorOriginal = original[campo];

        // Comparación especial para fechas
        if (valorActual instanceof Date || valorOriginal instanceof Date) {
          if (this.compararFechas(valorActual as Date, valorOriginal as Date)) {
            return true;
          }
        }
        // Comparación para strings (manejar null/undefined como strings vacíos)
        else if (typeof valorActual === 'string' || typeof valorOriginal === 'string') {
          if ((valorActual || '') !== (valorOriginal || '')) {
            return true;
          }
        }
        // Comparación directa para otros tipos
        else if (valorActual !== valorOriginal) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Compara dos objetos simples para detectar cambios en campos específicos
   */
  compararObjetos<T>(objeto1: T, objeto2: T, campos: (keyof T)[]): boolean {
    if (!objeto1 && !objeto2) return false;
    if (!objeto1 || !objeto2) return true;

    for (const campo of campos) {
      const valor1 = objeto1[campo];
      const valor2 = objeto2[campo];

      // Comparación especial para fechas
      if (valor1 instanceof Date || valor2 instanceof Date) {
        if (this.compararFechas(valor1 as Date, valor2 as Date)) {
          return true;
        }
      }
      // Comparación para strings (manejar null/undefined como strings vacíos)
      else if (typeof valor1 === 'string' || typeof valor2 === 'string') {
        if ((valor1 || '') !== (valor2 || '')) {
          return true;
        }
      }
      // Comparación directa para otros tipos
      else if (valor1 !== valor2) {
        return true;
      }
    }

    return false;
  }

  /**
   * Formatea una fecha para mostrar solo día, mes y año en formato DD-MM-YYYY (sin hora)
   * @param fecha Fecha a formatear (puede ser Date, string o null/undefined)
   * @returns Fecha formateada como string DD-MM-YYYY
   */
  formatearFechaSinHora(fecha: string | Date | null | undefined): string {
    if (!fecha) return '';

    let fechaObj: Date;

    // Si es string, crear objeto Date
    if (typeof fecha === 'string') {
      fechaObj = new Date(fecha);
    } else {
      fechaObj = fecha;
    }

    // Verificar que la fecha sea válida
    if (isNaN(fechaObj.getTime())) {
      console.warn('Fecha inválida recibida:', fecha);
      return '';
    }

    const dia = fechaObj.getDate().toString().padStart(2, '0');
    const mes = (fechaObj.getMonth() + 1).toString().padStart(2, '0');
    const anio = fechaObj.getFullYear();

    return `${dia}-${mes}-${anio}`;
  }

  /**
   * Convierte una hora en formato string "HH:mm" a un número en formato H.MM
   * Ejemplo: "01:30" → 1.30 (no 1.5). Este formato es solo representativo, no matemáticamente exacto.
   * @param tiempo Hora en formato string "HH:mm"
   * @returns Número con formato H.MM (por ejemplo, 2.45 para "02:45"). Retorna 0 si el formato es inválido.
   */
  convertirHoraStringAHoraPunto(tiempo: string): number {
    // Separar el string en horas y minutos usando el separador ':'
    const [horasStr, minutosStr] = tiempo.split(':');

    // Convertir los valores a enteros
    const horas = parseInt(horasStr, 10);
    const minutos = parseInt(minutosStr, 10);

    // Validar que ambos valores sean números válidos
    if (isNaN(horas) || isNaN(minutos)) {
      return 0; // En caso de error, se retorna 0
    }

    // Combinar horas y minutos en un string con formato "H.MM"
    // Asegura que los minutos siempre tengan dos dígitos (ej. 5 → "05")
    const formato = `${horas}.${minutos.toString().padStart(2, '0')}`;

    // Convertir el string a número decimal y retornarlo
    return parseFloat(formato);
  }

  base64ToText(base64: string): string {
    return Buffer.from(base64, "base64").toString("utf-8");
  }

  ordenarLista<T>(
    lista: T[],
    propiedad: keyof T,
    direccion: 'asc' | 'desc' = 'asc'
  ): T[] {
    return [...lista].sort((a, b) => {
      const valorA = this.normalizarValor(a[propiedad]);
      const valorB = this.normalizarValor(b[propiedad]);

      if (valorA == null && valorB == null) return 0;
      if (valorA == null) return direccion === 'asc' ? -1 : 1;
      if (valorB == null) return direccion === 'asc' ? 1 : -1;

      if (valorA > valorB) return direccion === 'asc' ? 1 : -1;
      if (valorA < valorB) return direccion === 'asc' ? -1 : 1;

      return 0;
    });
  }

  obtenerClasificacionIMC(imc: number | null | undefined): string | null {
    if (imc === null || imc === undefined) return null;

    if (imc < 18.5) return 'Bajo peso';
    if (imc >= 18.5 && imc <= 24.999) return 'Normal / Saludable';
    if (imc >= 25 && imc <= 29.999) return 'Sobrepeso (Pre-obesidad)';
    if (imc >= 30 && imc <= 34.999) return 'Obesidad clase I';
    if (imc >= 35 && imc <= 39.999) return 'Obesidad clase II';
    if (imc >= 40) return 'Obesidad clase III';

    return null;
  }

  vincularClasificacionIMC(
    form: FormGroup,
    nombreControlIMC: string,
    nombreControlClasificacion: string
  ): void {
    const controlIMC = form.get(nombreControlIMC);
    const controlClasificacion = form.get(nombreControlClasificacion);

    if (!controlIMC || !controlClasificacion) return;

    controlIMC.valueChanges.subscribe((valor: number) => {
      const imc = Number(valor);
      const clasificacion = this.obtenerClasificacionIMC(imc);
      controlClasificacion.setValue(clasificacion, { emitEvent: false });
    });

    // 👇 opcional: inicializar si ya tiene valor
    const valorInicial = controlIMC.value;
    if (valorInicial !== null && valorInicial !== undefined) {
      const clasificacion = this.obtenerClasificacionIMC(valorInicial);
      controlClasificacion.setValue(clasificacion, { emitEvent: false });
    }
  }

  private normalizarValor(valor: unknown): unknown {
    if (typeof valor === 'string') {
      return valor.trim().toLowerCase();
    }

    return valor;
  }

}