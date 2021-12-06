import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ServiceBenchmarkMatchService } from 'app/entities/service-benchmark-match/service-benchmark-match.service';
import { IServiceBenchmarkMatch, ServiceBenchmarkMatch } from 'app/shared/model/service-benchmark-match.model';

describe('Service Tests', () => {
  describe('ServiceBenchmarkMatch Service', () => {
    let injector: TestBed;
    let service: ServiceBenchmarkMatchService;
    let httpMock: HttpTestingController;
    let elemDefault: IServiceBenchmarkMatch;
    let expectedResult: IServiceBenchmarkMatch | IServiceBenchmarkMatch[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      injector = getTestBed();
      service = injector.get(ServiceBenchmarkMatchService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new ServiceBenchmarkMatch(0, 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should return a list of ServiceBenchmarkMatch', () => {
        const returnedFromService = Object.assign(
          {
            rationale: 'BBBBBB',
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
