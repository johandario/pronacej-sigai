import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FugaComponent } from './fuga.component';

describe('FugaComponent', () => {
  let component: FugaComponent;
  let fixture: ComponentFixture<FugaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FugaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FugaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
