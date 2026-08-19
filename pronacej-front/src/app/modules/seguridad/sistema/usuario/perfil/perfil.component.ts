import { Component, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatDrawer, MatSidenavModule } from '@angular/material/sidenav';
import { PanelDrawer } from "app/core/model/internos/panelDrawer.model";
import { CuentaUsuarioComponent } from "./cuenta-usuario/cuenta-usuario.component";
import { Subject } from "rxjs";
import { NgClass } from '@angular/common';
import { ActivatedRoute } from "@angular/router";
import { SeguridadContraseniaComponent } from "./seguridad-contrasenia/seguridad-contrasenia.component";

@Component(
    {
        selector: "app-perfil",
        templateUrl: "./perfil.component.html",
        standalone: true,
        imports: [
            MatButtonModule,
            MatIconModule,
            MatSidenavModule,
            CuentaUsuarioComponent,
            NgClass,
            SeguridadContraseniaComponent
        ]
    }
)
export class PerfilComponent implements OnInit, OnDestroy {

    @ViewChild('drawer') drawer: MatDrawer;
    drawerMode: 'over' | 'side' = 'side';
    drawerOpened: boolean = true;

    panels: PanelDrawer[] = [];
    selectedPanel: string = 'cuenta';
    private _unsubscribeAll: Subject<any> = new Subject<any>();

    tokenIdentificadorUsuario: string;

    constructor(
        private activatedRoute: ActivatedRoute
    ) {

    }

    ngOnInit(): void {
        this.tokenIdentificadorUsuario = this.activatedRoute.snapshot.queryParamMap.get("id");

        this.panels = [
            {
                id: "cuenta",
                icono: 'heroicons_outline:user-circle',
                titulo: "Cuenta",
                descripcion: "Administra los datos de tu cuenta"
            },
            {
                id: "seguridad",
                icono: 'heroicons_outline:lock-closed',
                titulo: "Seguridad",
                descripcion: "Administra la contraseña de tu cuenta"
            }
        ]
    }

    /**
    * Get the details of the panel
    *
    * @param id
    */
    getPanelInfo(id: string): any {
        return this.panels.find((panel) => panel.id === id);
    }

    /**
     * Navigate to the panel
     *
     * @param panel
     */
    goToPanel(panel: string): void {
        this.selectedPanel = panel;

        // Close the drawer on 'over' mode
        if (this.drawerMode === 'over') {
            this.drawer.close();
        }
    }

    /**
    * On destroy
    */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    /**
     * Track by function for ngFor loops
     *
     * @param index
     * @param item
     */
    trackByFn(index: number, item: any): any {
        return item.id || index;
    }
}