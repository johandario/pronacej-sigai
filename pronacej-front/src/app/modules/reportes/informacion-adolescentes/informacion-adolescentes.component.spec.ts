import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BackendService } from 'app/core/services/backend.service';
import { DialogMensajeService } from 'app/core/services/dialog-mensaje.service';
import { FichaIdentificacionService } from 'app/modules/administracion/services/fichaIdentificacion.service';
import { ReporteService } from 'app/modules/seguridad/services/reporte.service';

import { InformacionAdolescentesComponent } from './informacion-adolescentes.component';

describe('InformacionAdolescentesComponent', () => {
  let component: InformacionAdolescentesComponent;
  let fixture: ComponentFixture<InformacionAdolescentesComponent>;
  let reporteServiceMock: jasmine.SpyObj<ReporteService>;
  let fichaIdentificacionServiceMock: jasmine.SpyObj<FichaIdentificacionService>;

  beforeEach(async () => {
    reporteServiceMock = jasmine.createSpyObj('ReporteService', ['exportarAdolescentes', 'checkError']);
    reporteServiceMock.exportarAdolescentes.and.returnValue(of(new ArrayBuffer(8)));
    reporteServiceMock.checkError.and.resolveTo('');
    fichaIdentificacionServiceMock = jasmine.createSpyObj('FichaIdentificacionService', ['obtenerNombresFichas', 'checkError']);
    fichaIdentificacionServiceMock.obtenerNombresFichas.and.returnValue(of({ data: [] } as any));

    await TestBed.configureTestingModule({
      imports: [InformacionAdolescentesComponent],
      providers: [
        { provide: ReporteService, useValue: reporteServiceMock },
        { provide: FichaIdentificacionService, useValue: fichaIdentificacionServiceMock },
        {
          provide: BackendService,
          useValue: jasmine.createSpyObj('BackendService', ['desencriptarBdyEncriptado'])
        },
        {
          provide: DialogMensajeService,
          useValue: jasmine.createSpyObj('DialogMensajeService', ['mensajeErrorConTitulo'])
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformacionAdolescentesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call export service on export action', () => {
    component.adolescentesDisponibles = [
      {
        tokenIdentificador: 'token-1',
        etiqueta: '12345678 - Perez Lopez Juan',
        ficha: { numeroIdentificacion: '12345678' } as any,
      },
      {
        tokenIdentificador: 'token-2',
        etiqueta: '87654321 - Gomez Diaz Ana',
        ficha: { numeroIdentificacion: '87654321' } as any,
      },
    ];
    component.adolescentesSeleccionados = new Set(['token-1', 'token-2']);
    component.menusSeleccionados = new Set(['MENU_FICHA_PRINCIPAL', 'MENU_FICHA_INGRESO']);

    component.exportarAdolescentes();

    expect(reporteServiceMock.exportarAdolescentes).toHaveBeenCalledWith(
      jasmine.objectContaining({
        numerosIdentificacion: ['12345678', '87654321'],
        nemonicosSecciones: ['MENU_FICHA_PRINCIPAL', 'MENU_FICHA_INGRESO'],
      }),
      component.nemonicoMenu
    );
    expect(component.exportando).toBeFalse();
  });
});
