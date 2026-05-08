import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ProfileService } from './profile.service';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(ProfileService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should cache contributor summaries for 15 minutes', () => {
    const nowSpy = spyOn(Date, 'now').and.returnValue(1000);
    const contributors = [
      {
        id: 'contributor-1',
        displayName: 'Cached Contributor',
        profilePictureUrl: '',
        ratingCount: 4,
        blogPostCount: 1
      }
    ];
    let firstResponse: unknown;
    let secondResponse: unknown;

    service.listContributors().subscribe((response) => firstResponse = response);
    httpTesting.expectOne('/api/profiles/contributors').flush(contributors);

    nowSpy.and.returnValue(1000 + 14 * 60 * 1000);
    service.listContributors().subscribe((response) => secondResponse = response);

    httpTesting.expectNone('/api/profiles/contributors');
    expect(firstResponse).toEqual(contributors);
    expect(secondResponse).toEqual(contributors);
  });

  it('should refresh contributor summaries after 15 minutes', () => {
    const nowSpy = spyOn(Date, 'now').and.returnValue(1000);

    service.listContributors().subscribe();
    const firstRequest = httpTesting.expectOne('/api/profiles/contributors');
    expect(firstRequest.request.method).toBe('GET');
    firstRequest.flush([]);

    nowSpy.and.returnValue(1000 + 15 * 60 * 1000 + 1);
    service.listContributors().subscribe();

    const refreshedRequest = httpTesting.expectOne('/api/profiles/contributors');
    expect(refreshedRequest.request.method).toBe('GET');
    refreshedRequest.flush([]);
  });
});
