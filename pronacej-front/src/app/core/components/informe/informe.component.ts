import { CommonModule } from '@angular/common';
import { Component, Inject, Input, LOCALE_ID, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { CampoInformeDTO } from 'app/core/model/both/informe/campoInformeDTO.model';
import { InformeDTO } from 'app/core/model/both/informe/informeDTO.model';
import { TablaPlantilla } from 'app/core/model/internos/tablaPlantilla.model';
import { GeneracionPdfRequest } from 'app/core/model/request/GeneracionPdfRequest.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { PdfService } from 'app/core/services/pdf.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';
import { InformeService } from 'app/modules/general/services/informe.service';

@Component({
  selector: 'app-informe',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatSelectModule,
    MatCardModule,
    MatRadioModule
  ],
  templateUrl: './informe.component.html',
  styleUrl: './informe.component.scss'
})
export class InformeComponent {
  @Input() nemonicoInforme!: string;
  @Input() informe: InformeDTO;

  private nemonicoMenu = etiquetasModel.NEMONICO_MENU_INFORME;
  private esEdicion: boolean = false;

  formulario: FormGroup;
  formFields: CampoInformeDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private informeService: InformeService,
    private dialogMensajeService: DialogMensajeService,
    private dateAdapter: DateAdapter<any>,
    @Inject(LOCALE_ID) private locale: string) {
    this.dateAdapter.setLocale('es');
  }

  ngOnInit() {
    this.formulario = this.fb.group({});
    this.formFields.forEach(campo => {
      this.formulario.addControl(campo.idCampo.toString(), this.fb.control('', Validators.required));
    });

    if (this.informe)
      this.esEdicion = true;

    if (this.esEdicion)
      this.cargarCamposPorInforme();
    else
      this.cargarCamposPorNemonico();
  }

  ngOnChanges(changes: SimpleChanges) {
    if ((changes['nemonicoInforme'] && changes['nemonicoInforme'].currentValue) ||
      (changes['informe'] && changes['informe'].currentValue)) {

      if (this.informe)
        this.esEdicion = true;

      if (this.esEdicion)
        this.cargarCamposPorInforme();
      else
        this.cargarCamposPorNemonico();
    }
  }

  private cargarCamposPorNemonico() {

    this.informeService.obtenerCamposPorNemonico(this.nemonicoInforme, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<CampoInformeDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.formFields = response.data;

        this.formulario = this.fb.group({});
        this.formFields.forEach(campo => {
          this.formulario.addControl(campo.idCampo.toString(), this.fb.control('', Validators.required));
        });
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }

  private cargarCamposPorInforme() {

    this.informeService.obtenerCamposPorInforme(this.informe, this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<CampoInformeDTO[]>) => {

        if (!response.exito) {
          this.dialogMensajeService.mensajeError(
            'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
          );
          return;
        }

        this.formFields = response.data;

        this.formulario = this.fb.group({});
        this.formFields.forEach(campo => {
          this.formulario.addControl(campo.idCampo.toString(), this.fb.control(campo.valor, Validators.required));
        });

        if (this.esEdicion)
          this.formulario.disable();
        else
          this.formulario.enable();
      },
      error: (error: any) => {
        this.dialogMensajeService.mensajeError(
          'Hubo un problema al recuperar los registros. Inténtalo de nuevo.'
        );
      }
    });
  }
}
