import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrasladoDirectorAprobacionComponent } from './traslado-director-aprobacion.component';

describe('TrasladoDirectorAprobacionComponent', () => {
  let component: TrasladoDirectorAprobacionComponent;
  let fixture: ComponentFixture<TrasladoDirectorAprobacionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrasladoDirectorAprobacionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrasladoDirectorAprobacionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
