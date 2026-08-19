import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
    MatDrawerContainer,
    MatSidenavModule,
} from '@angular/material/sidenav';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
    NodeItem,
    TreeNgxComponent,
    TreeNgxModule,
} from 'app/core/components/tree-ngx';
import etiquetasModel from 'app/core/etiquetas.model';
import { CatalogoDTO } from 'app/core/model/both/catalogoDTO.model';
import { RespuestaPorDefecto } from 'app/core/model/response/RespuestaPorDefecto.model';
import { CatalogoService } from 'app/core/services/catalogo.service';
import { environment } from 'environments/environment';
import { InformacionCatalogoComponent } from '../informacion-catalogo/informacion-catalogo.component';
import { ModalInfoCatalogoComponent } from '../modal-info-catalogo/modal-info-catalogo.component';

@Component({
    selector: 'app-admin-catalogo',
    standalone: true,
    imports: [
        MatSidenavModule,
        MatIconModule,
        MatButtonModule,
        RouterModule,
        MatFormFieldModule,
        MatInputModule,
        FormsModule,
        MatCardModule,
        TreeNgxModule,
    ],
    templateUrl: './admin-catalogo.component.html',
    styleUrl: './admin-catalogo.component.scss',
})
export class AdminCatalogoComponent implements OnInit {
    catalogosPrincipales: CatalogoDTO[] = [];
    nodeItems: NodeItem<CatalogoDTO>[];

    catalogoEnUso: CatalogoDTO;

    searchTerm: string;

    compInformacionCatalogo: InformacionCatalogoComponent;

    // VARIABLE LOCAL SIMPLE
    nemonicoMenu = etiquetasModel.MENU_CATALOGOS;

    @ViewChild('treeNgx') treeNgxComp: TreeNgxComponent;
    @ViewChild('matDrawerContainer') matDrawerContainer: MatDrawerContainer;

    constructor(
        private catalogoService: CatalogoService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private matDialog: MatDialog
    ) {}
    
    ngOnInit(): void {
        this.obtenerCatalogosPrincipales();
    }

    clickArrowRigthEvent(node: NodeItem<CatalogoDTO>) {
        let catalogoDTO = node.item;
        this.catalogoService
            .obtenerHijos2(catalogoDTO.tokenIdentificador, this.nemonicoMenu)
            .subscribe({
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
                            node.id
                        );
                    }

                    this.matDrawerContainer.autosize = true;
                },
            });
    }

    verificarCatalogoEnUso(node: NodeItem<CatalogoDTO>) {
        let catalogoDTO = node.item;
        let result =
            catalogoDTO?.tokenIdentificador ==
            this.activatedRoute?.firstChild?.snapshot.paramMap.get(
                'token_catalogo'
            );

        return result;
    }

    usar(node: NodeItem<CatalogoDTO>) {
        this.catalogoEnUso = node.item;

        this.cambiarRuta(this.catalogoEnUso.tokenIdentificador);
    }

    private cambiarRuta(tokenCatalogo: string) {
        this.router
            .navigateByUrl(
                '/seguridad/sistema/catalogos' +
                    (tokenCatalogo ? '/' + tokenCatalogo : '')
            )
            .then((result: boolean) => {
                if (result) {
                    this.compInformacionCatalogo?.obtenerInformacionCatalogo();
                    this.matDrawerContainer.autosize = true;
                }
            });
    }

    convertirCatalogosANodeItem(catalogoDTO: CatalogoDTO, expanded = false) {
        let nodeITem: NodeItem<CatalogoDTO> = {
            name: catalogoDTO.nombre,
            item: catalogoDTO,
            id: catalogoDTO.tokenIdentificador,
            hasChild: catalogoDTO.tieneHijos,
            children: catalogoDTO.tieneHijos ? [] : null,
            expanded: expanded,
        };

        return nodeITem;
    }

    obtenerCatalogosPrincipales() {
        this.catalogoService.obtenerCatalogosPrincipales(this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
                if (!environment.production) {
                    console.log(response);
                }

                    if (!response.exito) {
                        this.catalogoService.checkError(response);
                    }

                    this.catalogosPrincipales = response.data;
                    this.nodeItems = this.catalogosPrincipales.map((cat) =>
                        this.convertirCatalogosANodeItem(cat)
                    );

                    if (this.matDrawerContainer) {
                        this.matDrawerContainer.autosize = true;
                    }
                },
                error: (error: any) => {
                    console.error(error);
                },
            });
    }

    onSearch() {
        let filtro = this.searchTerm;

        this.catalogoService.obtenerTodosPorString(filtro, this.nemonicoMenu).subscribe({
            next: (response: RespuestaPorDefecto<CatalogoDTO[]>) => {
                if (!environment.production) {
                    console.log(response);
                }

                if (!response.exito) {
                    this.catalogoService.checkError(response);
                    return;
                }

                this.nodeItems = [];
                this.treeNgxComp.treeService.clear();
                for (let cataloDTO of response.data) {
                    this.nodeItems.push(
                        this.convertirCatalogosANodeItem(cataloDTO)
                    );
                }
            },
        });
    }

    onRefresh() {
        this.obtenerCatalogosPrincipales();
    }

    buscarInput() {}

    verificarPadreEHijos(node: NodeItem<CatalogoDTO>) {
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

    onAdd() {
        let ref = this.matDialog.open(ModalInfoCatalogoComponent, {
            panelClass: ['w-full'],
        });

        ref.componentInstance.titulo = 'Crea un catalogo principal';
        ref.componentInstance.nemonicoMenu = this.nemonicoMenu;

        ref.afterClosed().subscribe({
            next: (response: boolean) => {
                if (response) {
                    this.onRefresh();
                }
            },
        });
    }

    activateRoute(componentRef: any) {
        this.compInformacionCatalogo = componentRef;
        
        this.compInformacionCatalogo.nemonicoMenu = this.nemonicoMenu;
        
        this.compInformacionCatalogo?.eliminarEvent.subscribe({
            next: (response: boolean) => {
                if (response) {
                    let parentNode = this.treeNgxComp.getParentById(
                        this.catalogoEnUso?.tokenIdentificador
                    );
                    this.treeNgxComp.deleteById(
                        this.catalogoEnUso.tokenIdentificador
                    );
                    this.cambiarRuta(
                        this.catalogoEnUso?.tokenIdentificadorPadre
                    );

                    parentNode.hasChild = parentNode.children?.length > 0;

                    this.catalogoEnUso = parentNode.item;
                    this.matDrawerContainer.autosize = true;
                }
            },
        });
        this.compInformacionCatalogo?.editarEvent.subscribe({
            next: (response: boolean) => {
                if (response) {
                    this.updateEvent();
                }
            },
        });

        this.compInformacionCatalogo?.descendenciaEvent.subscribe({
            next: (response: CatalogoDTO[]) => {
                if (response && response?.length > 0) {
                    for (let i = 0; response.length > i; i++) {
                        let catalogo = response[i];
                        let nodeItem = this.convertirCatalogosANodeItem(
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

        this.compInformacionCatalogo?.obtencionCatalogoActualEvent.subscribe({
            next: (response: CatalogoDTO) => {
                if (response) {
                    this.catalogoEnUso = response;
                }
            },
        });
    }

    private updateEvent() {
        let parentNode = this.treeNgxComp?.getParentById(
            this.catalogoEnUso.tokenIdentificador
        );

        if (parentNode) {
            this.clickArrowRigthEvent(parentNode);
        } else {
            this.onRefresh();
        }
    }
}