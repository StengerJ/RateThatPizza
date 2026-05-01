import { provideHttpClient } from '@angular/common/http';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Navbar } from './navbar';

describe('Navbar', () => {
  let component: Navbar;
  let fixture: ComponentFixture<Navbar>;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Navbar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render public navigation links', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const linkText = Array.from(compiled.querySelectorAll('a')).map((link) =>
      link.textContent?.trim()
    );

    expect(linkText).toContain('Home');
    expect(linkText).toContain('PGH-Pizza');
    expect(linkText).toContain('Ratings');
    expect(linkText).toContain('Blog');
    expect(linkText).toContain('Apply');
    expect(linkText).toContain('About');
    expect(linkText).toContain('Login');
  });
});
