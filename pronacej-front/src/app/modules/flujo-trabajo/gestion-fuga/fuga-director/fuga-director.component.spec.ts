import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FugaDirectorComponent } from './fuga-director.component';

describe('FugaDirectorComponent', () => {
  let component: FugaDirectorComponent;
  let fixture: ComponentFixture<FugaDirectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FugaDirectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FugaDirectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
