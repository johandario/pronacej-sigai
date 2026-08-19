import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuariosVerComponent } from './usuarios-ver.component';

describe('UsuariosVerComponent', () => {
  let component: UsuariosVerComponent;
  let fixture: ComponentFixture<UsuariosVerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosVerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuariosVerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
