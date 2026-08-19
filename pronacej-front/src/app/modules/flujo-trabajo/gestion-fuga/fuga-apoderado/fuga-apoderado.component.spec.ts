import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FugaApoderadoComponent } from './fuga-apoderado.component';

describe('FugaApoderadoComponent', () => {
  let component: FugaApoderadoComponent;
  let fixture: ComponentFixture<FugaApoderadoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FugaApoderadoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FugaApoderadoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
