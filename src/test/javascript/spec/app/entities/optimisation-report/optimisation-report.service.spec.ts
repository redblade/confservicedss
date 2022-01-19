import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OptimisationReportService } from 'app/entities/optimisation-report/optimisation-report.service';
import { IOptimisationReport, OptimisationReport } from 'app/shared/model/optimisation-report.model';

describe('Service Tests', () => {
  describe('OptimisationReport Service', () => {
    let injector: TestBed;
    let service: OptimisationReportService;
    let httpMock: HttpTestingController;
    let elemDefault: IOptimisationReport;
    let expectedResult: IOptimisationReport | IOptimisationReport[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      injector = getTestBed();
      service = injector.get(OptimisationReportService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new OptimisationReport(0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 0, 0, 0, 0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should return a list of OptimisationReport', () => {
        const returnedFromService = Object.assign(
          {
            optimisationType: 'BBBBBB',
            appName: 'BBBBBB',
            serviceName: 'BBBBBB',
            servicePriority: 1,
            requestCpu: 1,
            requestMem: 1,
            startupTime: 1,
            node: 'BBBBBB',
            nodeCategory: 'BBBBBB',
            optimisationScore: 'BBBBBB',
            serviceProvider: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
