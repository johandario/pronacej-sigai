import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PostEgresoComponent } from './post-egreso.component';

describe('PostEgresoComponent', () => {
  let component: PostEgresoComponent;
  let fixture: ComponentFixture<PostEgresoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostEgresoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PostEgresoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
