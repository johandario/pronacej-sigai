import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SalidasTrasladosComponent } from './salidas-traslados.component';

describe('SalidasTrasladosComponent', () => {
  let component: SalidasTrasladosComponent;
  let fixture: ComponentFixture<SalidasTrasladosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SalidasTrasladosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SalidasTrasladosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
