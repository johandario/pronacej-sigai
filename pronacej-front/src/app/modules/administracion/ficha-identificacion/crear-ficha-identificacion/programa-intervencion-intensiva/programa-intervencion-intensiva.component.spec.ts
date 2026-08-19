import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProgramaIntervencionIntensivaComponent } from './programa-intervencion-intensiva.component';

describe('ProgramaIntervencionIntensivaComponent', () => {
  let component: ProgramaIntervencionIntensivaComponent;
  let fixture: ComponentFixture<ProgramaIntervencionIntensivaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProgramaIntervencionIntensivaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProgramaIntervencionIntensivaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
