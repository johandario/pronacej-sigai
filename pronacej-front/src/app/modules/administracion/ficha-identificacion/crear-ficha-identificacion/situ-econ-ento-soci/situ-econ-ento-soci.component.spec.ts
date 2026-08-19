import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SituEconEntoSociComponent } from './situ-econ-ento-soci.component';

describe('SituEconEntoSociComponent', () => {
  let component: SituEconEntoSociComponent;
  let fixture: ComponentFixture<SituEconEntoSociComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SituEconEntoSociComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SituEconEntoSociComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
