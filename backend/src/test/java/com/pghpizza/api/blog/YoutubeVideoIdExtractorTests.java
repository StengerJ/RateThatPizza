package com.pghpizza.api.blog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class YoutubeVideoIdExtractorTests {
    @Test
    void extractsIdsFromCommonYoutubeInputs() {
        assertThat(YoutubeVideoIdExtractor.extract("https://www.youtube.com/watch?v=Cstdq9o8TO8"))
                .isEqualTo("Cstdq9o8TO8");
        assertThat(YoutubeVideoIdExtractor.extract("https://youtu.be/Cstdq9o8TO8"))
                .isEqualTo("Cstdq9o8TO8");
        assertThat(YoutubeVideoIdExtractor.extract("https://www.youtube.com/shorts/Cstdq9o8TO8?si=abc"))
                .isEqualTo("Cstdq9o8TO8");
        assertThat(YoutubeVideoIdExtractor.extract("v=Cstdq9o8TO8")).isEqualTo("Cstdq9o8TO8");
        assertThat(YoutubeVideoIdExtractor.extract("?v=Cstdq9o8TO8")).isEqualTo("Cstdq9o8TO8");
        assertThat(YoutubeVideoIdExtractor.extract("watch?v=Cstdq9o8TO8")).isEqualTo("Cstdq9o8TO8");
    }

    @Test
    void rejectsNonYoutubeInputs() {
        assertThat(YoutubeVideoIdExtractor.extract("https://example.com/watch?v=Cstdq9o8TO8")).isNull();
        assertThat(YoutubeVideoIdExtractor.extract("not-a-valid-video-id")).isNull();
    }
}
