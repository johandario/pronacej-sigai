import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubidaDocumentoGenericoComponent } from './subida-documento-generico.component';

describe('SubidaDocumentoGenericoComponent', () => {
  let component: SubidaDocumentoGenericoComponent;
  let fixture: ComponentFixture<SubidaDocumentoGenericoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubidaDocumentoGenericoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubidaDocumentoGenericoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
