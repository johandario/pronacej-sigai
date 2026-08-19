import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class TabService {
    private tabIndex = new BehaviorSubject<number>(0);
    tabIndex$ = this.tabIndex.asObservable();

    cambiarTab(index: number) {
        this.tabIndex.next(index);
    }
}
