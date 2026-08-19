import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetallesArchivoComponent } from './detalles-archivo.component';

describe('DetallesArchivoComponent', () => {
  let component: DetallesArchivoComponent;
  let fixture: ComponentFixture<DetallesArchivoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetallesArchivoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetallesArchivoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
