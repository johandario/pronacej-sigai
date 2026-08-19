import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VistaSalidaTrasladosComponent } from './vista-salida-traslados.component';

describe('VistaSalidaTrasladosComponent', () => {
  let component: VistaSalidaTrasladosComponent;
  let fixture: ComponentFixture<VistaSalidaTrasladosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VistaSalidaTrasladosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VistaSalidaTrasladosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
