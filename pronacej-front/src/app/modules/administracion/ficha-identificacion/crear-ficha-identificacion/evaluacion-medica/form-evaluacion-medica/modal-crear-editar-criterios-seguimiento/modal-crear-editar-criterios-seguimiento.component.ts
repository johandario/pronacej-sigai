import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogModule, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { CriterioEvaluacionMedicaSeguimientoDTO } from 'app/core/model/both/criterioEvaluacionMedicaSeguimientoDTO.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FuncionesUtils } from 'app/core/utils/funcionesUtils.model';

@Component({
  selector: 'app-modal-crear-editar-criterios-seguimiento',
  standalone: true,
  imports: [MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatButtonModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatIconModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatSelectModule,
    CommonModule,
    MatSlideToggleModule],
  templateUrl: './modal-crear-editar-criterios-seguimiento.component.html',
  styleUrl: './modal-crear-editar-criterios-seguimiento.component.scss'
})
export class ModalCrearEditarCriteriosSeguimientoComponent {

  listaEvaluaciones: CatalogoDTO[] = [];
  listaCriterios: CatalogoDTO[] = [];

  ingresoCriterioSeguimientoForm = this.fb.group({
    tipoEvaluacion: [null, [Validators.required]],
    tipoCriterio: [null, [Validators.required]],
    detalle: [null, [Validators.required]],
    id_temporal: []
  });

  constructor(private fb: FormBuilder,
    private cd: ChangeDetectorRef,
    private catalogoService: CatalogoService,
    public dialogRef: MatDialogRef<ModalCrearEditarCriteriosSeguimientoComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogMensajeService: DialogMensajeService,
    public funcionesUtils: FuncionesUtils,) {

  }

  async ngOnInit(): Promise<void>  {
    await this.cargarCatalogos();
    if (this.data.informacion) {
      console.log('informacion desde pag', this.data.informacion)
      this.ingresoCriterioSeguimientoForm.get('tipoEvaluacion').setValue(this.data.informacion.tokenIdentifidorCriterioPadre);
      this.ingresoCriterioSeguimientoForm.get('detalle').setValue(this.data.informacion.detalle);
      
      if(this.data.informacion.id_temporal){
        this.ingresoCriterioSeguimientoForm.get('id_temporal').setValue(this.data.informacion.id_temporal);
      }
      
      

    }
  }

  async cargarCatalogos() {
    this.funcionesUtils.obtenerListaCatalogo('FICHA_SALUD_SEGUIMIENTO', "").subscribe({
      next: (data) =>{
        this.listaEvaluaciones = data;
        if (this.data.informacion) {
          let nemonicoDTO = this.listaEvaluaciones.find(x=>x.tokenIdentificador==this.data.informacion.tokenIdentifidorCriterioPadre);
            this.funcionesUtils.obtenerListaCatalogo(nemonicoDTO.nemonico, "").subscribe({
              next: (data) => {
                this.listaCriterios = data;
                this.ingresoCriterioSeguimientoForm.get('tipoCriterio').setValue(this.data.informacion.tokenIdentificadorCriterioHijo);
        },
        error: (error) => console.error('Error cargando nemonicos:', error)
      });
        }
      } ,
      error: (error) => console.error('Error cargando nemonicos:', error)
    });
  }

  consultarCriterios(event: any) {
    let nemonico = event.value;
    let nemonicoDTO = this.listaEvaluaciones.find(x=>x.tokenIdentificador==nemonico);
    this.ingresoCriterioSeguimientoForm.get('tipoCriterio').setValue(null)
    this.funcionesUtils.obtenerListaCatalogo(nemonicoDTO.nemonico, "").subscribe({
      next: (data) => this.listaCriterios = data,
      error: (error) => console.error('Error cargando grados de instrucción:', error)
    });
  }

  registrarCriterio(){
    if (this.ingresoCriterioSeguimientoForm.valid) {
      let criterio = new CriterioEvaluacionMedicaSeguimientoDTO();
      criterio.tokenIdentifidorCriterioPadre = this.ingresoCriterioSeguimientoForm.get('tipoEvaluacion').value;
      criterio.tokenIdentificadorCriterioHijo = this.ingresoCriterioSeguimientoForm.get('tipoCriterio').value;
      criterio.detalle = this.ingresoCriterioSeguimientoForm.get('detalle').value;
      criterio.nombreCriterioHijo = this.listaCriterios.find(x => x.tokenIdentificador == criterio.tokenIdentificadorCriterioHijo).nombre;
      criterio.nombreCriterioPadre = this.listaEvaluaciones.find(x => x.tokenIdentificador == criterio.tokenIdentifidorCriterioPadre).nombre;

      if (this.data.informacion) {
        if(this.data.informacion.tokenIdentificador){
          criterio.tokenIdentificador = this.data.informacion.tokenIdentificador;
        }
        criterio.esEdicion = true;
        criterio.id_temporal = this.ingresoCriterioSeguimientoForm.get('id_temporal').value

      } else {
        criterio.id_temporal = Date.now();
      }
      this.dialogRef.close(criterio);
    }
  }

  cerrar() {
    this.dialogRef.close(false);
}

}
