import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogModule,
    MatDialogRef,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { InformacionUbicacionDTO } from 'app/core/model/both/InformacionUbicacionDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { InformacionUbicacionService } from 'app/modules/seguridad/services/informacionUbicacion.service';
import { environment } from 'environments/environment';

@Component({
    selector: 'app-modal-editar-informacion',
    standalone: true,
    imports: [
        MatFormFieldModule,
        MatInputModule,
        FormsModule,
        MatButtonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatIconModule,
        MatDialogModule,
        ReactiveFormsModule,
        MatSelectModule,
        CommonModule
    ],
    templateUrl: './modal-editar-informacion.component.html',
    styleUrl: './modal-editar-informacion.component.scss',
})
export class ModalEditarInformacionComponent implements OnInit {
    tiposInformacion: CatalogoDTO[];

    ingresoInformacionUbicacionForm = this.fb.group({
        tipoInformacion: [null],
        valor: [null],
    });
    

    constructor(
        private fb: FormBuilder,
        private cd: ChangeDetectorRef,
        private catalogoService: CatalogoService,
        public dialogRef: MatDialogRef<ModalEditarInformacionComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private dialogMensajeService: DialogMensajeService,
        private informacionUbicacionService: InformacionUbicacionService,
    ) {}

    ngOnInit(): void {
      this.obtenerListaCatalogo('TIPO_INFORMACION_PERSONAL');
      if(this.data.informacion){
        this.ingresoInformacionUbicacionForm.get('valor').setValue(this.data.informacion.valor);
        this.ingresoInformacionUbicacionForm.get('tipoInformacion').setValue(this.data.informacion.tipoInformacionUbicacion);
        console.log('datos a editar',this.data);
      }
      // this.cd.detectChanges();
    }

    obtenerListaCatalogo(nemonicoPadre: string) {
        this.catalogoService
            .obtenerHijos(
                nemonicoPadre,
                etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOSOCIAL
            )
            .subscribe({
                next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
                    if (!environment.production) {
                        // console.log(response);
                    }

                    if (!response.exito) {
                        this.dialogMensajeService.mensajeErrorConTitulo(
                            response.titulo,
                            response.mensaje
                        );
                        return;
                    }

                    this.tiposInformacion = response.data;
                },
                error: (error: any) => {
                    console.log(error);
                },
            });
    }

    ejecutarAccionInformacion() {
        this.ingresoInformacionUbicacionForm.disable();

        let informacion = new InformacionUbicacionDTO();

        informacion.valor = this.ingresoInformacionUbicacionForm.get('valor').value;
        informacion.tipoInformacionUbicacion =this.ingresoInformacionUbicacionForm.get('tipoInformacion').value;
        informacion.idPersonaRelacionada = this.data.idPersonaRelacionada;
        informacion.esEdicion = false;

        if(this.data.informacion){
          informacion.tokenIdentificador = this.data.informacion.tokenIdentificador;
          informacion.esEdicion = true;
        }

        this.informacionUbicacionService
            .crearInformacionUbicacion(
                informacion,
                etiquetasModel.NEMONICO_MENU_EVALUACION_PSICOSOCIAL
            )
            .subscribe({
                next: (
                    response: RespuestaPorDefecto<InformacionUbicacionDTO>
                ) => {
                    this.ingresoInformacionUbicacionForm.enable();

                    // this.completoOperacion.emit(response.exito);
                    if (!response.exito) {
                        this.informacionUbicacionService.checkError(response);

                        return;
                    }
                    this.dialogMensajeService.mensajeExitoso(
                        response.titulo,
                        response.mensaje
                    );
                    this.ingresoInformacionUbicacionForm.enable();
                    this.ingresoInformacionUbicacionForm.reset();
                    this.dialogRef.close(true);
                },
                error: (error: any) => {
                    this.informacionUbicacionService.checkError(error);
                    this.ingresoInformacionUbicacionForm.enable();
                    this.dialogRef.close(false);
                },
            });
    }

    registroUbicacion(){
        if(this.ingresoInformacionUbicacionForm.valid){
            let informacion = new InformacionUbicacionDTO();
            informacion.valor = this.ingresoInformacionUbicacionForm.get('valor').value;
            informacion.tipoInformacionUbicacion =this.ingresoInformacionUbicacionForm.get('tipoInformacion').value;
            informacion.idPersonaRelacionada = this.data.idPersonaRelacionada;
            informacion.nombreTipoInformacion = this.tiposInformacion.find(x=>x.nemonico==informacion.tipoInformacionUbicacion).nombre;

            if(this.data.informacion){
                informacion.tokenIdentificador = this.data.informacion.tokenIdentificador;
                informacion.esEdicion = true;
            }else{
                informacion.id_temporal = Date.now();
            }


            this.dialogRef.close(informacion);
        }
    }

    cerrar(){
        this.dialogRef.close(false);
    }
}
