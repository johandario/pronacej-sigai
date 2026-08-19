import { trigger, state, style, transition, animate } from '@angular/animations';

export const CustomSnackBarAnimations = [
  trigger('snackBarAnimation', [
    state('void', style({ opacity: 0, transform: 'translateX(100%)' })),
    state('*', style({ opacity: 1, transform: 'translateX(0)' })),
    transition('void => *', [
      animate('400ms ease-out')
    ]),
    transition('* => void', [
      animate('300ms ease-in', style({ opacity: 0, transform: 'translateX(100%)' }))
    ])
  ])
  ];
