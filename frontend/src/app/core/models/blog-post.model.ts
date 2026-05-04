export interface BlogPost {
  id?: string;
  authorId?: string;
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
