import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { Injectable } from "@angular/core";
import { FuseConfirmationConfig, FuseConfirmationService } from "@fuse/services/confirmation";
import { FuseConfirmationDialogComponent } from "@fuse/services/confirmation/dialog/dialog.component";

@Injectable(
    {
        providedIn: "root"
    }
)
export class DialogMensajeService {

    private _defaultConfig: FuseConfirmationConfig = {
        title: 'Confirm action',
        message: 'Are you sure you want to confirm this action?',
        icon: {
            show: true,
            name: 'heroicons_outline:exclamation-triangle',
            color: 'warn',
        },
        actions: {
            confirm: {
                show: true,
                label: 'Aceptar',
                color: 'primary',
            },
            cancel: {
                show: false,
                label: "Cancelar"
            },
        },
        dismissible: false,
    };

    constructor(private fuseConfirmationService: FuseConfirmationService) { }

    private mostrarMensaje(titulo: string, subtitulo: string, icono: 'heroicons_outline:check-circle'
        | 'heroicons_outline:x-circle' | 'heroicons_outline:exclamation-triangle',
        color: 'primary' | 'accent' | 'warn' = "primary",
        showCancel?: boolean): MatDialogRef<FuseConfirmationDialogComponent> {
        this._defaultConfig.title = titulo;
        this._defaultConfig.message = subtitulo;
        this._defaultConfig.icon.name = icono;
        this._defaultConfig.icon.color = color;
        this._defaultConfig.actions.confirm.show = true;
        this._defaultConfig.actions.cancel.show = showCancel;

        let ref = this.fuseConfirmationService.open(this._defaultConfig);
        return ref;
    }

    mensajeConConfirmacion(titulo: string, subtitulo: string) {
        this._defaultConfig.title = titulo;
        this._defaultConfig.message = subtitulo;
        this._defaultConfig.icon.name = "heroicons_outline:exclamation-triangle";
        this._defaultConfig.icon.color = "info";
        this._defaultConfig.actions.cancel.show = true;
        this._defaultConfig.actions.confirm.show = true;

        let ref = this.fuseConfirmationService.open(this._defaultConfig);
        return ref;
    }

    mensajeLoading(texto = "Realizando operación") {
        this._defaultConfig.title = "";
        this._defaultConfig.message = texto;
        this._defaultConfig.icon.name = "heroicons_outline:ellipsis-horizontal";
        this._defaultConfig.icon.color = "info";
        this._defaultConfig.actions.cancel.show = false;
        this._defaultConfig.actions.confirm.show = false;

        this._defaultConfig.dismissible = false;
        let ref = this.fuseConfirmationService.open(this._defaultConfig);
        return ref;
    }

    private mostrarMensajeExitoso2(titulo: string, subtitulo: string) {
        return this.mostrarMensaje(titulo, subtitulo, 'heroicons_outline:check-circle', "primary", false);
    }

    mensajeExitoso(titulo: string, subtitulo: string): MatDialogRef<FuseConfirmationDialogComponent> {
        return this.mostrarMensajeExitoso2(titulo, subtitulo);
    }

    mensajeError(subtitulo: string): MatDialogRef<FuseConfirmationDialogComponent> {
        return this.mostrarMensaje("Petición fallida", subtitulo, 'heroicons_outline:x-circle', "warn");
    }

    mensajeErrorConTitulo(titulo: string, subtitulo: string): MatDialogRef<FuseConfirmationDialogComponent> {
        return this.mostrarMensaje(titulo, subtitulo, 'heroicons_outline:x-circle', "warn");
    }

    mensajeAdvertencia(titulo: string, subtitulo: string) {
        const config = { ...this._defaultConfig };

        config.title = titulo;
        config.message = subtitulo;
        config.icon.name = "heroicons_outline:exclamation-triangle";
        config.icon.color = "warning";
        config.actions.cancel.show = false;
        config.actions.confirm.show = true;
        config.actions.confirm.label = "Aceptar";
        let ref = this.fuseConfirmationService.open(config);
        return ref;
    }
}