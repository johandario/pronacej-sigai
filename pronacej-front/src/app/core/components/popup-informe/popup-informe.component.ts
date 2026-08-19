import { Component, Inject, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import etiquetasModel from 'app/core/etiquetas.model';
import { PopupDocumentosComponent } from '../documentos/popup-documentos/popup-documentos.component';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformeService } from 'app/modules/general/services/informe.service';
import { InformeDTO } from 'app/core/model/both/informe/informeDTO.model';
import { ActivatedRoute } from '@angular/router';
import { ValorInformeDTO } from 'app/core/model/both/informe/valorInformeDTO.model';
import { InformeComponent } from '../informe/informe.component';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { FichaIdentificacionDTO } from 'app/core/model/both/fichaIdentificacionDTO.model';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { HttpClient } from '@angular/common/http';
import { PdfService } from 'app/core/services/pdf.service';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-popup-informe',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    InformeComponent
  ],
  templateUrl: './popup-informe.component.html',
  styleUrl: './popup-informe.component.scss'
})
export class PopupInformeComponent {
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME;

  @ViewChild(InformeComponent) informe!: InformeComponent;

  uuid_fp: string;
  nemonicoInforme: string;

  base64Image: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<PopupDocumentosComponent>,
    private dialogMensajeService: DialogMensajeService,
    private informeService: InformeService,
    private fichaService: FichaIdentificacionService,
    private funcionesUtils: FuncionesUtils,
    private pdfService: PdfService,
    private http: HttpClient,
    private route: ActivatedRoute,
    @Inject(MAT_DIALOG_DATA) public data: { nemonicoInforme: string, uuid_fp: string }
  ) {
    this.nemonicoInforme = data.nemonicoInforme;
    this.uuid_fp = data.uuid_fp;
  }

  guardar() {
    this.loadImageAsBase64();
    console.log(this.uuid_fp);
    let informeDTO = new InformeDTO();

    informeDTO.tokenFichaIdentificacion = this.uuid_fp;
    informeDTO.nemonicoPlantillaInforme = this.nemonicoInforme;
    informeDTO.impreso = true;
    informeDTO.valores = this.obtenerValores();

    this.informeService.crearInformePorToken(informeDTO, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<InformeDTO>) => {
        console.log(response);
        this.dialogMensajeService.mensajeExitoso(
          'Guardar',
          'Informe guardado correctamente.'
        ).afterClosed().subscribe(() => {
          this.imprimir(response.data);
          this.dialogRef.close();
        });
      },
      error: (err) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al guardar el informe. Inténtalo de nuevo.'
        );
      }
    });
  }

  obtenerValores(): ValorInformeDTO[] {
    const valores: ValorInformeDTO[] = [];

    Object.keys(this.informe.formulario.controls).forEach(key => {
      const control = this.informe.formulario.get(key);

      // Crear el ValorInformeDTO para cada campo
      const valorInformeDTO: ValorInformeDTO = {
        idCampo: +key,
        valor: String(control?.value || '')
      };

      valores.push(valorInformeDTO);
    });

    return valores;
  }

  imprimir(informe: InformeDTO) {
    console.log(informe);
    this.fichaService.obtenerFichaIdentificacionPorTokenIdentificador(this.uuid_fp, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<FichaIdentificacionDTO>) => {
          if (!response.exito) {
            return;
          }

          const fichaDTO = response.data;

          // Construir los nuevos campos dinámicamente
          const lugarFechaNacimiento = `${fichaDTO.lugarNacimiento || ''}, ${this.formatFecha(fichaDTO.fechaNacimiento)}`;
          const edadActual = this.funcionesUtils.getEdad(fichaDTO.fechaNacimiento).toString() || 'N/A';
          const gradoInstruccion = fichaDTO.gradoInstruccion || 'N/A';
          const direccion = fichaDTO.direccion || 'N/A';

          let request = new GeneracionPdfRequest();
          request.nemonico = etiquetasModel.FORMULARIO_INFORME;
          request.variables = {
            "[IMG_BASE64]": this.base64Image,
            "[FECHA_REGISTRO]": this.formatFecha(informe.fechaRegistro.toString()),
            "[HORA_REGISTRO]": this.formatHora(informe.fechaRegistro.toString()),
            "[TITULO-INFORME]": informe.tipo,
            "[ADOLESCENTE]": informe.asignado,
            "[LUGAR_FECHA_NACIMIENTO]": lugarFechaNacimiento,
            "[CENTRO]": fichaDTO.centroIngreso,
            "[EDAD_ACTUAL]": edadActual,
            "[GRADO_INSTRUCCION]": gradoInstruccion,
            "[DIRECCION]": direccion,
            "[INFORME]": informe.idInforme.toString(),
          }
          this.pdfService.generarPdf(request, this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<string>) => {

              if (!response.exito) {
                this.dialogMensajeService.mensajeError(
                  'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
                );
                return;
              }

              const url = window.URL.createObjectURL(this.funcionesUtils.getPdfBlob(response.data));

              const pwa = window.open(url);
            },
            error: (error: any) => {
              this.dialogMensajeService.mensajeError(
                'Hubo un problema al generar el archivo. Inténtalo de nuevo.'
              );
            }
          });

        },
        error: (error: any) => {
          this.fichaService.checkError(error);
        }
      }
    );
  }

  cancelar() {
    this.dialogRef.close();
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    });
  }

  formatHora(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleTimeString('es-ES');
  }

  // Función para cargar la imagen como base64
  loadImageAsBase64() {
    this.http.get('images/logo/logo.png', { responseType: 'arraybuffer' })
      .subscribe((data: ArrayBuffer) => {
        const base64String = this.arrayBufferToBase64(data);
        this.base64Image = `data:image/png;base64,${base64String}`;
      });
  }

  // Función para convertir el ArrayBuffer a base64
  arrayBufferToBase64(buffer: ArrayBuffer): string {
    const binary = String.fromCharCode(...new Uint8Array(buffer));
    return window.btoa(binary);
  }

}
