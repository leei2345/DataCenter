package com.jinba.spider.core;

/**
 * 标记request使用显式字节流还是字符串
 * @author leei
 *
 */
public enum HttpRequestConfig
{
  RequestBodyAsString("RequestBodyAsString", true), 
  RequestBodyAsStream("RequestBodyAsStream", false),
  RequestBodyContainFile("RequestBodyContainFile", false);

  private String Config;
  private boolean YesOrNo;

  private HttpRequestConfig(String config, boolean yesorno) { this.Config = config;
    this.YesOrNo = yesorno; }

  public String getConfig()
  {
    return this.Config;
  }

  public void setConfig(String config) {
    this.Config = config;
  }

  public boolean isYesOrNo() {
    return this.YesOrNo;
  }

  public void setYesOrNo(boolean yesOrNo) {
    this.YesOrNo = yesOrNo;
  }
}