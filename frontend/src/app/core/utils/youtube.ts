const YOUTUBE_ID_PATTERN = /^[a-zA-Z0-9_-]{11}$/;

export function extractYoutubeVideoId(value: string | null | undefined): string | null {
  const candidate = value?.trim();

  if (!candidate) {
    return null;
  }

  if (YOUTUBE_ID_PATTERN.test(candidate)) {
    return candidate;
  }

  try {
    const url = new URL(candidate);
    const host = url.hostname.replace(/^www\./, '');

    if (host === 'youtu.be') {
      const id = url.pathname.split('/').filter(Boolean)[0];
      return id && YOUTUBE_ID_PATTERN.test(id) ? id : null;
    }

    if (host === 'youtube.com' || host === 'm.youtube.com') {
      const watchId = url.searchParams.get('v');

      if (watchId && YOUTUBE_ID_PATTERN.test(watchId)) {
        return watchId;
      }

      const embedMatch = url.pathname.match(/^\/embed\/([a-zA-Z0-9_-]{11})$/);
      return embedMatch ? embedMatch[1] : null;
    }
  } catch {
    return null;
  }

  return null;
}

export function buildYoutubeEmbedUrl(videoId: string): string {
  return `https://www.youtube.com/embed/${videoId}`;
}
