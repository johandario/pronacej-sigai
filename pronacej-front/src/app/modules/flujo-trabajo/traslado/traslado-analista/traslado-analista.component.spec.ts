import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrasladoAnalistaComponent } from './traslado-analista.component';

describe('TrasladoAnalistaComponent', () => {
  let component: TrasladoAnalistaComponent;
  let fixture: ComponentFixture<TrasladoAnalistaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrasladoAnalistaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrasladoAnalistaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
