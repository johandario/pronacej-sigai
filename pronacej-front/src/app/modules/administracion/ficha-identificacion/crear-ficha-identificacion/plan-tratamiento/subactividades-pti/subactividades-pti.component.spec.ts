import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubactividadesPtiComponent } from './subactividades-pti.component';

describe('SubactividadesPtiComponent', () => {
  let component: SubactividadesPtiComponent;
  let fixture: ComponentFixture<SubactividadesPtiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubactividadesPtiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubactividadesPtiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
