package com.team4.monew.entity;

public enum ArticleSource {
  HANKYUNG("한국경제", "https://www.hankyung.com/feed/all-news"),
  CHOSUN("조선일보", "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml"),
  YONHAP("연합뉴스", "http://www.yonhapnewstv.co.kr/browse/feed/");

  private final String source;
  private final String rssUrl;

  ArticleSource(String source, String rssUrl) {
    this.source = source;
    this.rssUrl = rssUrl;
  }

  public String getSource() {
    return source;
  }

  public String getRssUrl() {
    return rssUrl;
  }
}
