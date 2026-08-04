package com.leets7th.job_is_be.domain.mail.service;

import com.leets7th.job_is_be.domain.mail.config.MailProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MailTemplateRendererTest {

    @Test
    void rendersWelcomeAndDailyBriefingLinks() {
        MailProperties properties = new MailProperties();
        properties.setFrontendBaseUrl("https://jobis-app.com/");
        MailTemplateRenderer renderer = new MailTemplateRenderer(properties);

        String welcome = renderer.welcome();
        String daily = renderer.dailyBriefing(List.of(
                new MailTemplateRenderer.BriefingItem("백엔드 개발자", "Job.is", 92, "관심 직무와 맞아요")
        ), "unsubscribe-token");

        assertThat(welcome).contains("Job.is에 오신 걸 환영해요", "https://jobis-app.com/recommendations");
        assertThat(daily).contains(
                "백엔드 개발자",
                "적합도 92%",
                "https://jobis-app.com/recommendations/deck",
                "https://jobis-app.com/profile",
                "https://jobis-app.com/settings/notifications",
                "https://jobis-app.com/unsubscribe?token=unsubscribe-token"
        );
    }

    @Test
    void escapesDynamicHtml() {
        MailTemplateRenderer renderer = new MailTemplateRenderer(new MailProperties());

        String html = renderer.dailyBriefing(List.of(
                new MailTemplateRenderer.BriefingItem("<script>", "A&B", 50, "안전 < 우선")
        ), "token");

        assertThat(html).doesNotContain("<script>")
                .contains("&lt;script&gt;", "A&amp;B", "안전 &lt; 우선");
    }
}
