import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformeFinalComponent } from './informe-final.component';

describe('InformeFinalComponent', () => {
  let component: InformeFinalComponent;
  let fixture: ComponentFixture<InformeFinalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformeFinalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformeFinalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
