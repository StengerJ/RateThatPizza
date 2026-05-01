import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { BlogService } from '../../core/services/blog.service';
import { extractYoutubeVideoId } from '../../core/utils/youtube';

@Component({
  selector: 'app-blog-form-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './blog-form-page.component.html',
  styleUrls: ['./blog-form-page.component.css']
})
export class BlogFormPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly blogService = inject(BlogService);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(4)]],
    slug: [''],
    body: ['', [Validators.required, Validators.minLength(20)]],
    youtubeUrl: ['']
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal('');

  submit(): void {
    this.errorMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const trimmedYoutubeUrl = value.youtubeUrl.trim();
    const videoId = trimmedYoutubeUrl ? extractYoutubeVideoId(trimmedYoutubeUrl) : null;

    if (trimmedYoutubeUrl && !videoId) {
      this.errorMessage.set('Use a valid YouTube link or video ID.');
      return;
    }

    const slug = value.slug.trim() || this.slugify(value.title);

    this.submitting.set(true);
    this.blogService
      .createPost({
        title: value.title.trim(),
        slug,
        body: value.body.trim(),
        youtubeUrl: trimmedYoutubeUrl || undefined,
        youtubeVideoId: videoId ?? undefined
      })
      .subscribe({
        next: (post) => {
          this.submitting.set(false);
          void this.router.navigate(['/blog', post.slug]);
        },
        error: () => {
          this.errorMessage.set('Blog post could not be saved. Please try again later.');
          this.submitting.set(false);
        }
      });
  }

  private slugify(value: string): string {
    return value
      .trim()
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }
}
