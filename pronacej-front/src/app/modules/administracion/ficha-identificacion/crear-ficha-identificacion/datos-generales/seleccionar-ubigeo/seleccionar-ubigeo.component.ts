import { NestedTreeControl } from '@angular/cdk/tree';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogModule, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTreeModule, MatTreeNestedDataSource } from '@angular/material/tree';
import etiquetasModel from 'app/core/etiquetas.model';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { LocalidadService } from 'app/modules/seguridad/services/localidad.service';
import { environment } from 'environments/environment';
import { NodeItem, TreeNgxComponent, TreeNgxModule } from 'app/core/components/tree-ngx';
import { MatDrawerContainer, MatSidenavModule } from '@angular/material/sidenav';

@Component({
  selector: 'app-seleccionar-ubigeo',
  standalone: true,
  imports: [MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatTreeModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    MatSelectModule,
    CommonModule,
    TreeNgxModule,
    MatSidenavModule],
  templateUrl: './seleccionar-ubigeo.component.html',
  styleUrl: './seleccionar-ubigeo.component.scss'
})
export class SeleccionarUbigeoComponent implements OnInit {

  ingresoUbigeoForm = this.fb.group({
    ubigeo: [null],
  });

  dataSource = new MatTreeNestedDataSource<LocalidadDTO>();
  localidades: LocalidadDTO[] = [];
  treeControl = new NestedTreeControl<LocalidadDTO>((node) => node.hijos);

  childrenAccessor = (node: LocalidadDTO) => node.hijos ?? [];

  hasChild = (_: number, node: LocalidadDTO) => node.tieneHijos || (node.hijos && node.hijos.length > 0);

  @ViewChild("treeNgx") treeNgxComp: TreeNgxComponent;

  isLoading = true;
  nodeItems: NodeItem<LocalidadDTO>[] = [];

  seleccionoNodo(nodo: LocalidadDTO) {
    console.log('nodo', nodo)
  }

  @ViewChild("matDrawerContainer") matDrawerContainer: MatDrawerContainer;

  constructor(private localidadService: LocalidadService,
    private dialogMensajeService: DialogMensajeService,
    public dialogRef: MatDialogRef<SeleccionarUbigeoComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder,
  ) {


  }

  ngOnInit(): void {
    this.obtenerLocalidadesPrincipales();
    setTimeout(() => {
      if (!this.treeNgxComp) {
        console.warn("⚠️ tree-ngx no se ha inicializado correctamente.");
      } else {
        console.log("✅ tree-ngx cargado correctamente.");
      }
    }, 500);
    this.updateTreeData();

  }



  obtenerLocalidadesPorPadre(nemonicoPadre: string, nemonicoTipo: string) {
    console.log('datos', nemonicoPadre + nemonicoTipo);
    this.localidadService
      .obtenerHijos(
        nemonicoPadre,
        etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
      )
      .subscribe({
        next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
          if (!environment.production) {
            console.log('resp localidad', response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }

          this.localidades = response.data;
          this.dataSource.data = response.data;
          this.treeControl.dataNodes = this.dataSource.data;
          this.treeControl.collapseAll();
          this.isLoading = false;

          // if (nemonicoTipo === 'PAIS') {
          //   this.paises = response.data;
          // } else if (nemonicoTipo === 'DEPARTAMENTO') {
          //   this.departamentos = response.data;
          // } else if (nemonicoTipo === 'PROVINCIA') {
          //   this.provincias = response.data;
          // } else if (nemonicoTipo === 'DISTRITO') {
          //   this.distritos = response.data;
          // }
        },
        error: (error: any) => {
          console.log(error);
          this.isLoading = false;
        },
      });
  }

  // loadChildren(node: LocalidadDTO) {
  //   if (!node.hijos || node.hijos.length === 0) {
  //     this.localidadService
  //       .obtenerHijos(node.nemonico, etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL)
  //       .subscribe({
  //         next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
  //           if (response.exito) {
  //             // Agrega los hijos al nodo expandido
  //             node.hijos = response.data;

  //             // Actualiza el árbol manteniendo los nodos existentes
  //             this.updateTreeData();
  //             this.treeControl.expand(node);
  //             console.log('datasource', this.dataSource)
  //           } else {
  //             this.dialogMensajeService.mensajeErrorConTitulo(
  //               response.titulo,
  //               response.mensaje
  //             );
  //           }
  //         },
  //         error: (error: any) => {
  //           console.error('Error al cargar hijos:', error);
  //         },
  //       });
  //   } else {
  //     this.treeControl.toggle(node); // Alterna el estado de expansión si ya tiene hijos
  //   }
  // }

  loadChildren(node: LocalidadDTO) {
    if (!node.hijos || node.hijos.length === 0) {
      this.localidadService
        .obtenerHijos(node.nemonico, etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL)
        .subscribe({
          next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
            console.log('Hijos recibidos:', response);
            if (response.exito) {
              // Asignamos los nuevos hijos al nodo
              node.hijos = response.data;
              node.tieneHijos = response.data.length > 0;

              // Forzamos la actualización del árbol
              this.updateTreeData();
              this.treeControl.expand(node);
            } else {
              this.dialogMensajeService.mensajeErrorConTitulo(response.titulo, response.mensaje);
            }
          },
          error: (error: any) => {
            console.error('Error al cargar hijos:', error);
          },
        });
    } else {
      this.treeControl.toggle(node); // Alterna la expansión si ya tiene hijos cargados
    }
  }


  listarLocalidad(nemonicoTipo: string) {
    this.localidadService
      .obtenerPorTipo(
        nemonicoTipo,
        etiquetasModel.NEMONICO_MENU_EVALUACION_SOCIAL
      )
      .subscribe({
        next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
          if (!environment.production) {
            console.log('hijos', response);
          }

          if (!response.exito) {
            this.dialogMensajeService.mensajeErrorConTitulo(
              response.titulo,
              response.mensaje
            );
            return;
          }
          this.dataSource.data = response.data;
        },
        error: (error: any) => {
          console.log(error);
        },
      });
  }

  buscarNodoPorNemonico(nemonico: string, nodos: LocalidadDTO[] = this.dataSource.data, hijos?: LocalidadDTO[]): LocalidadDTO | null {
    // Itera sobre cada nodo
    for (let node of nodos) {
      // Si el nodo tiene el nemonico que estamos buscando, lo retorna
      if (node.nemonico === nemonico) {
        node.hijos = hijos;
        console.log('encontro', node)
        return node;
      }

      // Si el nodo tiene hijos, busca recursivamente en ellos
      if (node.hijos && node.hijos.length > 0) {
        const encontrado = this.buscarNodoPorNemonico(nemonico, node.hijos);
        if (encontrado) {
          return encontrado;
        }
      }
    }

    // Si no encuentra el nodo, retorna null
    return null;
  }

  reemplazarNodoPorNemonico(nemonico: string, nodos: LocalidadDTO[], hijos: LocalidadDTO[]): boolean {
    for (let node of nodos) {
      if (node.nemonico === nemonico) {
        node.hijos = hijos; // Actualiza los hijos del nodo encontrado
        this.dataSource.data = [...this.dataSource.data]; // Clona el array para notificar cambios
        return true;
      }
      if (node.hijos && node.hijos.length > 0) {
        const reemplazado = this.reemplazarNodoPorNemonico(nemonico, node.hijos, hijos);
        if (reemplazado) {
          return true;
        }
      }
    }
    return false;
  }

  // updateTreeData() {
  //   this.dataSource.data = [...this.dataSource.data]; // Clonamos el array para forzar actualización
  //   this.treeControl.dataNodes = this.dataSource.data; // Asignamos los nuevos nodos
  //   this.cdr.markForCheck(); // Forzamos la detección de cambios
  //   setTimeout(() => {
  //     this.cdr.detectChanges(); // Aplica cambios en el siguiente ciclo de Angular
  //   });
  //   console.log('Árbol actualizado:', this.dataSource.data);
  // }


  hasNestedChild(index: number, node: LocalidadDTO) {
    return node?.hijos?.length > 0;

  }

  cerrar() {
    this.dialogRef.close(false);
  }

  registrarUbigeo() {
    console.log('ubigeo', this.ingresoUbigeoForm.get('ubigeo').value)
  }

  seleccionar(node: NodeItem<LocalidadDTO>) {
    console.log('node', node)
    this.dialogRef.close(node.item.rutaUbigeo);
  }

  trackByNombre = (index: number, node: LocalidadDTO): string => {
    return node.nemonico || index.toString(); // Retorna el nemónico o el índice si está vacío
  };



  obtenerLocalidadesPrincipales() {
    this.localidadService.obtenerHijos("PAIS-PERU",'').subscribe({
      next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
        console.log("📌 Localidades recibidas:", response.data);
  
        if (response.exito && response.data.length > 0) {
          this.nodeItems = response.data.map(localidad => this.convertirLocalidadANodeItem(localidad));
        } else {
          console.warn("⚠️ No se recibieron localidades.");
        }
  
        this.isLoading = false;
  
        // 🔄 Forzar actualización del árbol
        setTimeout(() => {
          this.cdr.detectChanges();  // Detectar cambios en la vista
          console.log("✅ Árbol actualizado en la vista.");
        }, 100);
        setTimeout(() => {
          console.log("📌 nodeItems antes de renderizar el árbol:", this.nodeItems);
        }, 200);
      },
      error: (error: any) => {
        console.error("❌ Error al cargar localidades:", error);
        this.isLoading = false;
      }
    });
  }
  

  convertirLocalidadANodeItem(localidadDTO: LocalidadDTO): NodeItem<LocalidadDTO> {
    return {
      name: localidadDTO.nombre,
      item: localidadDTO,
      id: localidadDTO.tokenIdentificador,
      hasChild: localidadDTO.tieneHijos, // Indica si tiene hijos
      children: localidadDTO.tieneHijos ? [] : null,
      expanded: false,
    };
  }

  clickArrowRigthEvent(node: NodeItem<LocalidadDTO>) {
    let localidadDTO = node.item;
  
    this.localidadService.obtenerHijos(localidadDTO.nemonico,'').subscribe({
      next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
        if (response.exito) {
          for (let hijo of response.data) {
            this.treeNgxComp.deleteById(hijo.nemonico);
            this.treeNgxComp.addNodeById(this.convertirLocalidadANodeItem(hijo), node.id);
          }
          this.matDrawerContainer.autosize = true
        }
        
      },
      error: (error: any) => {
        console.error('❌ Error al cargar hijos:', error);
      }
    });
  }

  updateTreeData() {
    this.nodeItems = [...this.nodeItems]; // Clonar array para forzar detección de cambios
    this.cdr.detectChanges(); // Forzar actualización del DOM
    console.log(" Árbol actualizado:", this.nodeItems);
  }
}
