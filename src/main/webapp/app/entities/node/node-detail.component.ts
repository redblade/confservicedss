import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { INode } from 'app/shared/model/node.model';

@Component({
  selector: 'jhi-node-detail',
  templateUrl: './node-detail.component.html',
})
export class NodeDetailComponent implements OnInit {
  node: INode | null = null;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ node }) => (this.node = node));
  }

  previousState(): void {
    window.history.back();
  }
}
