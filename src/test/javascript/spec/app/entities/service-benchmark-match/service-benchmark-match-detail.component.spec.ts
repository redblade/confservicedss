import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ConfserviceTestModule } from '../../../test.module';
import { ServiceBenchmarkMatchDetailComponent } from 'app/entities/service-benchmark-match/service-benchmark-match-detail.component';
import { ServiceBenchmarkMatch } from 'app/shared/model/service-benchmark-match.model';

describe('Component Tests', () => {
  describe('ServiceBenchmarkMatch Management Detail Component', () => {
    let comp: ServiceBenchmarkMatchDetailComponent;
    let fixture: ComponentFixture<ServiceBenchmarkMatchDetailComponent>;
    const route = ({ data: of({ serviceBenchmarkMatch: new ServiceBenchmarkMatch(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [ConfserviceTestModule],
        declarations: [ServiceBenchmarkMatchDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }],
      })
        .overrideTemplate(ServiceBenchmarkMatchDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ServiceBenchmarkMatchDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should load serviceBenchmarkMatch on init', () => {
        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.serviceBenchmarkMatch).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
