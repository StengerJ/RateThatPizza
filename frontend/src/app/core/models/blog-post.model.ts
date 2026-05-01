export interface BlogPost {
  id?: string;
  title: string;
  slug: string;
  body: string;
  youtubeUrl?: string;
  youtubeVideoId?: string;
  author: string;
  createdAt: string;
}

export interface BlogPostCreateRequest {
  title: string;
  slug: string;
  body: string;
  youtubeUrl?: string;
  youtubeVideoId?: string;
}
