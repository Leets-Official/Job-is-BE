package com.leets7th.job_is_be.domain.mail.service;

import com.leets7th.job_is_be.domain.mail.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MailTemplateRenderer {

    private final MailProperties properties;

    public String welcome() {
        return page("Job.is에 오신 걸 환영해요",
                "무한 스크롤 대신, 매일 검토를 끝낸 공고를 이유와 함께 보내드려요.",
                """
                        <ul style="line-height:1.9;color:#374151;padding-left:22px">
                          <li>하루 3~6건, 왜 골랐는지 이유까지</li>
                          <li>관심 없으면 스킵, 마음에 들면 저장</li>
                          <li>스킵과 저장이 다음 추천을 더 정확하게</li>
                        </ul>
                        """,
                button("지금 첫 레터 보기", url(properties.getBriefingPath())),
                footer(null));
    }

    public String dailyBriefing(List<BriefingItem> items, String unsubscribeToken) {
        BriefingItem top = items.getFirst();
        StringBuilder cards = new StringBuilder();
        cards.append("<div style=\"border:1px solid #d1d5db;padding:20px;margin:20px 0\">")
                .append("<div style=\"font-size:12px;color:#6b7280\">").append(escape(top.companyName())).append("</div>")
                .append("<h2 style=\"margin:8px 0\">").append(escape(top.title())).append("</h2>")
                .append("<div style=\"color:#047857;font-weight:700\">적합도 ").append(top.fitScore()).append("%</div>")
                .append("<p style=\"background:#f3f4f6;padding:12px\">").append(escape(top.reason())).append("</p>")
                .append("</div>");
        if (items.size() > 1) {
            cards.append("<h3>함께 추천한 공고</h3><ul style=\"line-height:1.9\">");
            items.stream().skip(1).limit(3).forEach(item -> cards.append("<li>")
                    .append(escape(item.title())).append(" - ").append(escape(item.companyName())).append("</li>"));
            cards.append("</ul>");
        }
        return page("Job.is · 오늘의 레터",
                "좋은 저녁입니다. 오늘은 " + items.size() + "건을 추천드려요.",
                cards.toString(),
                button("오늘의 레터 열기", url(properties.getBriefingPath())),
                footer(unsubscribeToken));
    }

    private String page(String title, String intro, String content, String cta, String footer) {
        return """
                <!doctype html><html><body style="margin:0;background:#f4f5f7;font-family:Arial,sans-serif;color:#111827">
                <div style="max-width:640px;margin:0 auto;padding:32px 20px">
                  <div style="background:#ffffff;border:1px solid #e5e7eb;padding:32px">
                    <h1 style="font-size:24px;margin:0 0 18px">%s</h1>
                    <p style="line-height:1.7;color:#374151">%s</p>
                    %s
                    %s
                    %s
                  </div>
                </div></body></html>
                """.formatted(escape(title), escape(intro), content, cta, footer);
    }

    private String button(String label, String href) {
        return "<p style=\"text-align:center;margin:28px 0\"><a href=\"" + escape(href)
                + "\" style=\"display:inline-block;background:#111827;color:#fff;text-decoration:none;padding:14px 28px\">"
                + escape(label) + "</a></p>";
    }

    private String footer(String unsubscribeToken) {
        String unsubscribe = unsubscribeToken == null ? "" : " · <a href=\""
                + escape(url(properties.getUnsubscribePath()) + "?token="
                + URLEncoder.encode(unsubscribeToken, StandardCharsets.UTF_8)) + "\">수신거부</a>";
        return "<hr style=\"border:0;border-top:1px solid #e5e7eb;margin-top:32px\"><p style=\"font-size:12px;color:#6b7280\">"
                + "<a href=\"" + escape(url(properties.getProfilePath())) + "\">내 프로필</a> · "
                + "<a href=\"" + escape(url(properties.getNotificationSettingsPath())) + "\">수신 설정</a>"
                + unsubscribe + "</p>";
    }

    private String url(String path) {
        String base = properties.getFrontendBaseUrl().replaceAll("/+$", "");
        return base + (path.startsWith("/") ? path : "/" + path);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public record BriefingItem(String title, String companyName, int fitScore, String reason) {
    }
}
