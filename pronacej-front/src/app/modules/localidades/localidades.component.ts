import { CommonModule } from '@angular/common';
import { Component, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDrawerContainer, MatSidenavModule } from '@angular/material/sidenav';
import { MatTreeModule } from '@angular/material/tree';
import { NodeItem, TreeNgxComponent, TreeNgxModule } from 'app/core/components/tree-ngx';
import { LocalidadDTO } from 'app/core/model/both/localidadDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { LocalidadService } from '../seguridad/services/localidad.service';
import { environment } from 'environments/environment';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { InformacionLocalidadComponent } from './informacion-localidad/informacion-localidad.component';
import etiquetasModel from 'app/core/etiquetas.model';
import { ModalCreacionLocalidadesComponent } from './modal-creacion-localidades/modal-creacion-localidades.component';

@Component({
  selector: 'app-localidades',
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
    MatSidenavModule,
    MatSidenavModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    TreeNgxModule,
    RouterModule
  ],
  templateUrl: './localidades.component.html',
  styleUrl: './localidades.component.scss'
})
export class LocalidadesComponent implements OnInit {

  localidadesPrincipales: LocalidadDTO[] = [];
  nodeItems: NodeItem<LocalidadDTO>[];

  catalogoEnUso: LocalidadDTO;

  searchTerm: string;
  nemonicoMenu = etiquetasModel.MENU_LOCALIDADES;

  @ViewChild('treeNgx') treeNgxComp: TreeNgxComponent;
  @ViewChild('matDrawerContainer') matDrawerContainer: MatDrawerContainer;

  compInformacionLocalidadComponent: InformacionLocalidadComponent;
  catalogoActual: LocalidadDTO; 


  constructor(private localidadService: LocalidadService,
    private dialogMensajeService: DialogMensajeService,
    private fb: FormBuilder,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private matDialog: MatDialog,
  ) {


  }

  ngOnInit(): void {
    this.obtenerLocalidadesPrincipales();
    this.cargarCatalogoRaiz();
  }

  cargarCatalogoRaiz() {
  this.localidadService.obtenerLocalidadPorNemonico('PAIS-PERU', this.nemonicoMenu).subscribe({
    next: (resp: RespuestaPorDefecto<LocalidadDTO>) => {
      if (resp.exito && resp.data) {
        this.catalogoActual = resp.data;
        console.log(this.catalogoActual);
        
      } else {
        this.dialogMensajeService.mensajeError("No se encontró la localidad raíz PAIS-PERU.");
      }
    },
    error: (err) => {
      console.error(" Error al obtener PAIS-PERU:", err);
      this.dialogMensajeService.mensajeError("Error al cargar la localidad raíz.");
    }
  });
}

  obtenerLocalidadesPrincipales() {
    this.localidadService.obtenerHijos("PAIS-PERU", this.nemonicoMenu).subscribe({
      next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
        console.log("📌 Localidades recibidas:", response.data);

        if (response.exito && response.data.length > 0) {
          this.nodeItems = response.data.map(localidad => this.convertirLocalidadANodeItem(localidad));
        } else {
          console.warn("⚠️ No se recibieron localidades.");
        }

        this.localidadesPrincipales = response.data;
        this.nodeItems = this.localidadesPrincipales.map((cat) =>
          this.convertirLocalidadANodeItem(cat)
        );
        if (this.matDrawerContainer) {
          this.matDrawerContainer.autosize = true;
        }
      },
      error: (error: any) => {
        console.error("❌ Error al cargar localidades:", error);
      }
    });
  }

  convertirLocalidadANodeItem(catalogoDTO: LocalidadDTO, expanded = false) {
    let nodeITem: NodeItem<LocalidadDTO> = {
      name: catalogoDTO.nombre,
      item: catalogoDTO,
      id: catalogoDTO.tokenIdentificador,
      hasChild: catalogoDTO.tieneHijos,
      children: catalogoDTO.tieneHijos ? [] : null,
      expanded: expanded,
    };

    return nodeITem;
  }

  // clickArrowRigthEvent(node: NodeItem<LocalidadDTO>) {
  //   let catalogoDTO = node.item;
  //   this.localidadService
  //     .obtenerHijos(catalogoDTO.nemonico, this.nemonicoMenu)
  //     .subscribe({
  //       next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
  //         if (!environment.production) {
  //           console.log(response);
  //         }

  //         if (!response.exito) {
  //           console.log(response.mensaje);
  //           return;
  //         }

  //         for (let calogo of response.data) {
  //           this.treeNgxComp.deleteById(calogo.nemonico);
  //           this.treeNgxComp.addNodeById(
  //             this.convertirLocalidadANodeItem(calogo),
  //             node.id
  //           );
  //         }

  //         this.matDrawerContainer.autosize = true;
  //       },
  //     });
  // }


  clickArrowRigthEvent(node: NodeItem<LocalidadDTO>) {
  const catalogoDTO = node.item;

  this.localidadService
    .obtenerHijos(catalogoDTO.nemonico, this.nemonicoMenu)
    .subscribe({
      next: (response: RespuestaPorDefecto<LocalidadDTO[]>) => {
        if (!response.exito) {
          this.dialogMensajeService.mensajeError(response.mensaje);
          return;
        }

        const treeService = this.treeNgxComp.treeService;
        const nodeState = treeService['getNodeState'](
          treeService.treeState,
          node.id,
          (s, id) => s.nodeItem.id === id
        );

        if (nodeState) {
          // ❌ Eliminar hijos visualmente y del estado
          const hijosPrevios = [...(nodeState.children ?? [])];
          for (const child of hijosPrevios) {
            this.treeNgxComp.deleteById(child.nodeItem.id);
          }

          nodeState.children = [];
          nodeState.filteredChildren = [];
          nodeState.nodeItem.children = [];
        }

        // ✅ Convertir y agregar nuevos hijos
        const nuevosHijos = response.data.map(localidad =>
          this.convertirLocalidadANodeItem(localidad)
        );

        for (const hijo of nuevosHijos) {
          this.treeNgxComp.addNodeById(hijo, node.id);
        }

        // ✅ Actualizar estado interno del árbol
        nodeState.nodeItem.children = nuevosHijos;
        nodeState.nodeItem.hasChild = nuevosHijos.length > 0;
        nodeState.expanded = true;

            if (this.matDrawerContainer) {
        this.matDrawerContainer.autosize = true;
      }


      },
      error: () => {
        this.dialogMensajeService.mensajeError('Error al recargar hijos.');
      }
    });
}




  verificarCatalogoEnUso(node: NodeItem<LocalidadDTO>) {
    let catalogoDTO = node.item;
    let result =
      catalogoDTO?.tokenIdentificador ==
      this.activatedRoute?.firstChild?.snapshot.paramMap.get(
        'token_localidad'
      );

    return result;
  }

  verificarPadreEHijos(node: NodeItem<LocalidadDTO>) {
    let esActual = node?.id == this.catalogoEnUso?.tokenIdentificador;

    let nodeItemPadre = this.treeNgxComp?.getParentById(
      this.catalogoEnUso?.tokenIdentificador
    );
    let esPadreDelActual = false;

    while (nodeItemPadre && !esPadreDelActual) {
      esPadreDelActual = nodeItemPadre.id == node?.id;

      nodeItemPadre = this.treeNgxComp?.getParentById(nodeItemPadre?.id);
    }

    return esActual || esPadreDelActual;
  }

  // usar(node: NodeItem<LocalidadDTO>) {
  //   this.catalogoEnUso = node.item;

  //   this.cambiarRuta(this.catalogoEnUso.tokenIdentificador);
  // }

  usar(node: NodeItem<LocalidadDTO>) {
  this.catalogoEnUso = node.item;
  this.cambiarRuta(this.catalogoEnUso.tokenIdentificador);
}

  private cambiarRuta(tokenCatalogo: string) {
    this.router
      .navigateByUrl(
        '/seguridad/sistema/localidades' +
        (tokenCatalogo ? '/' + tokenCatalogo : '')
      )
      .then((result: boolean) => {
        if (result) {
          this.compInformacionLocalidadComponent?.obtenerInformacionLocalidad();
          this.matDrawerContainer.autosize = true;

        }
      });
  }

  activateRoute(componentRef: any) {
    this.compInformacionLocalidadComponent = componentRef;
    // this.compInformacionLocalidadComponent?.eliminarEvent.subscribe({
    //   next: (response: boolean) => {
    //     if (response) {
    //       let parentNode = this.treeNgxComp.getParentById(
    //         this.catalogoEnUso?.tokenIdentificador
    //       );
    //       this.treeNgxComp.deleteById(
    //         this.catalogoEnUso.tokenIdentificador
    //       );
    //       this.cambiarRuta(
    //         this.catalogoEnUso?.tokenIdentificadorPadre
    //       );

    //       parentNode.hasChild = parentNode.children?.length > 0;

    //       this.catalogoEnUso = parentNode.item;
    //       this.matDrawerContainer.autosize = true;
    //     }
    //   },
    // });
    this.compInformacionLocalidadComponent?.editarEvent.subscribe({
      next: (response: boolean) => {
        if (response) {
          this.updateEvent();
        }
      },
    });

    this.compInformacionLocalidadComponent?.descendenciaEvent.subscribe({
      next: (response: LocalidadDTO[]) => {
        if (response && response?.length > 0) {
          for (let i = 0; response.length > i; i++) {
            let catalogo = response[i];
            let nodeItem = this.convertirLocalidadANodeItem(
              catalogo,
              true
            );
            let parent = this.treeNgxComp.getParentById(
              nodeItem.id
            );

            if (!parent) {
              this.treeNgxComp.addNodeById(
                nodeItem,
                catalogo.tokenIdentificadorPadre
              );
            }

            this.treeNgxComp.expandById(
              catalogo.tokenIdentificadorPadre
            );
          }
        }
      },
    });

    this.compInformacionLocalidadComponent?.obtencionCatalogoActualEvent.subscribe({
      next: (response: LocalidadDTO) => {
        if (response) {
          this.catalogoEnUso = response;
        }
      },
    });
  }

  // private updateEvent() {
  //   let parentNode = this.treeNgxComp?.getParentById(
  //     this.catalogoEnUso.tokenIdentificador
  //   );

  //   if (parentNode) {
  //     this.clickArrowRigthEvent(parentNode);
  //   } else {
  //     this.onRefresh();
  //   }
  // }


  private updateEvent() {
  let parentNode = this.treeNgxComp?.getParentById(this.catalogoEnUso.tokenIdentificador);

  if (parentNode) {
    this.clickArrowRigthEvent(parentNode);
  } else {
    this.onRefresh();
  }

  // 👇 Redirige a la ruta actual para actualizar vista y router-outlet
  this.router.navigateByUrl(
    '/seguridad/sistema/localidades/' + this.catalogoEnUso.tokenIdentificador
  );
}


  onRefresh() {
    this.obtenerLocalidadesPrincipales();
}


addDepartment(){
  this.crearHijo()
}

 crearHijo() {
  if (!this.catalogoActual) {
    this.dialogMensajeService.mensajeError("No se ha cargado la localidad raíz.");
    return;
  }

  const ref = this.matDialog.open(ModalCreacionLocalidadesComponent, {
    panelClass: ['w-full']
  });

  ref.componentInstance.titulo = "Crear departamento:";
  ref.componentInstance.tipoLocalidad = "DEPARTAMENTO";
  ref.componentInstance.catalogoPadre = this.catalogoActual;

  // ref.afterClosed().subscribe({
  //   next: (resp: boolean) => {
  //     if (resp) {
  //       this.onRefresh(); 
  //     }
  //   }
  // });
  ref.afterClosed().subscribe({
  next: (resp: boolean) => {
    if (resp) {
      this.router.navigateByUrl('/seguridad/sistema/localidades');
    }
  }
});
}
}