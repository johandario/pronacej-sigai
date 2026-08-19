import { AfterViewInit, ChangeDetectorRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDrawerContainer, MatSidenavModule } from '@angular/material/sidenav';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterOutlet } from '@angular/router';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { environment } from 'environments/environment';
import { TFInformacionComponent } from '../t-f-informacion/t-f-informacion.component';
import { MatDividerModule } from '@angular/material/divider';
import { CatalogoService } from 'app/core/services/catalogo.service';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { NodeItem, TreeNgxComponent, TreeNgxModule } from 'app/core/components/tree-ngx';

@Component({
  selector: 'app-t-f-administrar',
  standalone: true,
  imports: [
    MatSidenavModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    RouterOutlet,
    MatDividerModule,
    TreeNgxModule
  ],
  templateUrl: './t-f-administrar.component.html',
  styleUrl: './t-f-administrar.component.scss'
})
export class TFAdministrarComponent implements OnInit, AfterViewInit {

  seccionesFichaPrincipal: CatalogoDTO[];

  nodeItems: NodeItem<CatalogoDTO>[] = [];

  declare tFInformacionComponent: TFInformacionComponent;
  seccionFichaPrincipalEnUso: CatalogoDTO;

  @ViewChild("treeNgx") treeNgxComp: TreeNgxComponent;
  @ViewChild("matDrawerContainer") matDrawerContainer: MatDrawerContainer;
  nemonicoMenu = etiquetasModel.NEMONICO_MENU_CONFIGURACION_TIPO_DE_ARCHIVOS_FICHA_PRINCIPAL;

  constructor(
    private router: Router,
    private changeDetectorRef: ChangeDetectorRef,
    private catalogoService: CatalogoService
  ) { }

  ngAfterViewInit(): void {

  }

  ngOnInit(): void {
    this.obternerSeccionesDeFichaPrincipal();
  }

  cargarHijos(nodeItem: NodeItem<CatalogoDTO>) {
    let catalogoDTO = nodeItem.item;

    this.catalogoService.obtenerHijos2(catalogoDTO.tokenIdentificador, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            console.log(response.mensaje);
            return;
          }

          for (let calogo of response.data) {
            this.treeNgxComp.deleteById(calogo.tokenIdentificador);
            this.treeNgxComp.addNodeById(
              this.convertirCatalogosANodeItem(calogo),
              nodeItem.id
            );
          }

          this.matDrawerContainer.autosize = true
        }
      }
    );
  }

  verificarCatalogoEnUso(node: NodeItem<CatalogoDTO>) {
    let catalogoDTO = node.item;
    let result = catalogoDTO?.tokenIdentificador == this.seccionFichaPrincipalEnUso?.tokenIdentificador;

    return result;
  }

  verificarPadreEHijos(node: NodeItem<CatalogoDTO>) {
    let esActual = node?.id == this.seccionFichaPrincipalEnUso?.tokenIdentificador;

    let nodeItemPadre = this.treeNgxComp?.getParentById(this.seccionFichaPrincipalEnUso?.tokenIdentificador);
    let esPadreDelActual = false;

    while (nodeItemPadre && !esPadreDelActual) {
      esPadreDelActual = nodeItemPadre.id == node?.id;

      nodeItemPadre = this.treeNgxComp?.getParentById(nodeItemPadre?.id);
    }

    return esActual || esPadreDelActual;
  }


  obternerSeccionesDeFichaPrincipal() {
    this.catalogoService.obtenerHijos(etiquetasModel.SECCIONES_FICHA_DE_IDENTIFICACION, this.nemonicoMenu).subscribe(
      {
        next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
          if (!environment.production) {
            console.log(response);
          }

          if (!response.exito) {
            this.catalogoService.checkError(response);
            return;
          }

          this.seccionesFichaPrincipal = response.data;
          this.nodeItems = this.seccionesFichaPrincipal.map(
            (seccion) => this.convertirCatalogosANodeItem(seccion)
          );

        },
        error: (error: any) => {
          this.catalogoService.checkError(error);
        }
      }
    );
  }

  private convertirCatalogosANodeItem(catalogoDTO: CatalogoDTO, expanded = false) {
    let nodeITem: NodeItem<CatalogoDTO> = {
      name: catalogoDTO.nombre,
      item: catalogoDTO,
      id: catalogoDTO.tokenIdentificador,
      hasChild: catalogoDTO.tieneHijos,
      children: catalogoDTO.tieneHijos ? [] : null,
      expanded: expanded
    }

    return nodeITem;
  }

  usar(nodeItem: NodeItem<CatalogoDTO>) {
    this.seccionFichaPrincipalEnUso = nodeItem.item;
    this.router.navigateByUrl(
      "/configuracion/tipo-de-documento-seccion-ficha-principal/" + this.seccionFichaPrincipalEnUso.tokenIdentificador
    ).then(
      (resp: boolean) => {
        if (resp) {
          this.tFInformacionComponent?.obtenerPorSeccionFichaPrincipal(
            this.seccionFichaPrincipalEnUso.tokenIdentificador,
          );

          this.tFInformacionComponent?.resetar();
        }
      }
    );
  }

  verificarSeccionEnUso(seccionfichaPrincipal: CatalogoDTO) {
    return seccionfichaPrincipal.tokenIdentificador == this.seccionFichaPrincipalEnUso?.tokenIdentificador;
  }

  activateRoute(activateComp: any) {
    this.tFInformacionComponent = activateComp;

    this.tFInformacionComponent?.descendenciaEvent.subscribe(
      {
        next: (response: CatalogoDTO[]) => {
          if (response && response?.length > 0) {
            for (let i = 0; response.length > i; i++) {
              let catalogo = response[i];
              let nodeItem = this.convertirCatalogosANodeItem(catalogo, true);
              let parent = this.treeNgxComp.getParentById(nodeItem.id);

              if (!parent) {
                this.treeNgxComp.addNodeById(
                  nodeItem,
                  catalogo.tokenIdentificadorPadre
                );
              }


              this.treeNgxComp.expandById(catalogo.tokenIdentificadorPadre);
            }
          }

          this.matDrawerContainer.autosize = true;
        }
      }
    );

    this.tFInformacionComponent.catalogoActualEvent.subscribe(
      {
        next: (catalogo: CatalogoDTO) => {
          this.seccionFichaPrincipalEnUso = catalogo;
        }
      }
    );
  }
}
