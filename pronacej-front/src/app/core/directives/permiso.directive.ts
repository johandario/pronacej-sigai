import { Directive, ElementRef, Input, OnDestroy, OnInit, TemplateRef, ViewContainerRef } from "@angular/core";
import { PermisoRolUsuarioService } from "app/modules/seguridad/services/permiso-rol-usuario.service";
import { Subject, takeUntil } from "rxjs";

@Directive({
    selector: '[appPermiso]',
    standalone: true
})
export class PermisoDirective implements OnInit, OnDestroy {

    private destroy$ = new Subject<void>();
    private hasView = false;

    @Input('appPermiso') config!: [string, string];

    constructor(
        private tpl: TemplateRef<any>,
        private vcr: ViewContainerRef,
        private permisosService: PermisoRolUsuarioService
    ) {}

    ngOnInit(): void {
        const [menu, accion] = this.config;

        this.permisosService
        .hasPermission$(menu, accion)
        .pipe(takeUntil(this.destroy$))
        .subscribe(permitido => {
            if (permitido && !this.hasView) {
            this.vcr.createEmbeddedView(this.tpl);
            this.hasView = true;
            } else if (!permitido && this.hasView) {
            this.vcr.clear();
            this.hasView = false;
            }
        });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    // @Input('appPermisoMenu') nemonicoMenu!: string;
    // @Input('appPermisoAccion') nemonicoAccion!: string;

    // private destroy$ = new Subject<void>();

    // constructor(
    //     private el: ElementRef,
    //     private permisosService: PermisoRolUsuarioService
    // ) {}

    // ngOnInit(): void {
    //     this.permisosService
    //         .hasPermission$(this.nemonicoMenu, this.nemonicoAccion)
    //         .pipe(takeUntil(this.destroy$))
    //         .subscribe(permitido => {
    //             if (!permitido) {
    //                 this.el.nativeElement.remove();
    //             }
    //         });
    // }

    // ngOnDestroy(): void {
    //     this.destroy$.next();
    //     this.destroy$.complete();
    // }
}
