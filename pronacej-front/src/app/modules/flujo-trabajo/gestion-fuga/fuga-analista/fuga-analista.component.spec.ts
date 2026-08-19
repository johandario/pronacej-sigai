import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FugaAnalistaComponent } from './fuga-analista.component';

describe('FugaAnalistaComponent', () => {
  let component: FugaAnalistaComponent;
  let fixture: ComponentFixture<FugaAnalistaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FugaAnalistaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FugaAnalistaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
