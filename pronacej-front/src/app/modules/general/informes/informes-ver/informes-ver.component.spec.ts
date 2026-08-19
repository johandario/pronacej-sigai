import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InformesVerComponent } from './informes-ver.component';

describe('InformesVerComponent', () => {
  let component: InformesVerComponent;
  let fixture: ComponentFixture<InformesVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformesVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InformesVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
