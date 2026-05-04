import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { RatingsPage } from './ratings-page.component';

describe('RatingsPage', () => {
  let fixture: ComponentFixture<RatingsPage>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [RatingsPage],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RatingsPage);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should request ratings from the planned Spring Boot API', () => {
    fixture.detectChanges();

    const request = httpTesting.expectOne('/api/ratings');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should render the required rating table columns', () => {
    fixture.detectChanges();
    httpTesting.expectOne('/api/ratings').flush([]);
    fixture.detectChanges();

    const nativeElement = fixture.nativeElement as HTMLElement;
    const headers = Array.from(nativeElement.querySelectorAll('th')).map((header) =>
      header.textContent?.trim()
    );

    expect(headers).toEqual([
      'Restaurant Name',
      'Location',
      'Sauce',
      'Toppings',
      'Crust',
      'Overall Rating',
      'Affordability Rating',
      'Contributor',
      'Comments'
    ]);
  });

  it('should show the empty state instead of an error box when ratings fail to load', () => {
    fixture.detectChanges();
    httpTesting.expectOne('/api/ratings').flush(null, {
      status: 500,
      statusText: 'Server Error'
    });
    fixture.detectChanges();

    const nativeElement = fixture.nativeElement as HTMLElement;

    expect(nativeElement.querySelector('.status.error')).toBeNull();
    expect(nativeElement.textContent).toContain('No ratings are available yet.');
  });
});
