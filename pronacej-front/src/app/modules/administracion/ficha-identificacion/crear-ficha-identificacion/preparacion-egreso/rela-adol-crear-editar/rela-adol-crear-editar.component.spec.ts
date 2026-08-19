import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RelaAdolCrearEditarComponent } from './rela-adol-crear-editar.component';

describe('RelaAdolCrearEditarComponent', () => {
  let component: RelaAdolCrearEditarComponent;
  let fixture: ComponentFixture<RelaAdolCrearEditarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RelaAdolCrearEditarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RelaAdolCrearEditarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
