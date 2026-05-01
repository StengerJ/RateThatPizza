import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { BlogPost } from '../../core/models/blog-post.model';
import { BlogService } from '../../core/services/blog.service';
import { buildYoutubeEmbedUrl, extractYoutubeVideoId } from '../../core/utils/youtube';

@Component({
  selector: 'app-blog-detail-page',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './blog-detail-page.component.html',
  styleUrls: ['./blog-detail-page.component.css']
})
export class BlogDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly blogService = inject(BlogService);
  private readonly sanitizer = inject(DomSanitizer);

  readonly post = signal<BlogPost | null>(null);
  readonly embedUrl = signal<SafeResourceUrl | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');

    if (!slug) {
      this.errorMessage.set('No blog post is available yet.');
      this.loading.set(false);
      return;
    }

    this.blogService
      .getPostBySlug(slug)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (post) => {
          this.post.set(post);
          this.embedUrl.set(this.createEmbedUrl(post));
        },
        error: () => this.errorMessage.set('No blog post is available yet.')
      });
  }

  private createEmbedUrl(post: BlogPost): SafeResourceUrl | null {
    const videoId = post.youtubeVideoId ?? extractYoutubeVideoId(post.youtubeUrl);

    if (!videoId) {
      return null;
    }

    return this.sanitizer.bypassSecurityTrustResourceUrl(buildYoutubeEmbedUrl(videoId));
  }
}
