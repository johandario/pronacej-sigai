import { Component, OnInit, Input, TemplateRef, Output, EventEmitter } from '@angular/core';
import { NodeState } from '../model/node-state';

@Component({
  selector: 'node-icon-wrapper',
  templateUrl: './node-icon-wrapper.component.html'
})
export class NodeIconWrapperComponent {

  @Input() state: NodeState;
  @Input() nodeCollapsibleTemplate: TemplateRef<any>;

  @Output() clickArrowRigthEvent = new EventEmitter<NodeState>();

  public _this = this;

  constructor() {
  }

  clickArrowRigth() {
    console.log(this.state);
    this.clickArrowRigthEvent.emit(this.state);
  }

  public toggleExpand() {
    this.state.expanded = !this.state.expanded;
  }
}
