import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrasladoDirectorComponent } from './traslado-director.component';

describe('TrasladoDirectorComponent', () => {
  let component: TrasladoDirectorComponent;
  let fixture: ComponentFixture<TrasladoDirectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrasladoDirectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrasladoDirectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
