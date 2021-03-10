package com.vlinkage.xunyee.utils.weixin;
 
public class Token
{
  private String accessToken;
  private int expiresIn;
  private long getTokenTime;
 
  public long getGetTokenTime()
  {
    return this.getTokenTime;
  }
 
  public void setGetTokenTime(long getTokenTime)
  {
    this.getTokenTime = getTokenTime;
  }
 
  public String getAccessToken()
  {
    return this.accessToken;
  }
 
  public void setAccessToken(String accessToken)
  {
    this.accessToken = accessToken;
  }
 
  public int getExpiresIn()
  {
    return this.expiresIn;
  }
 
  public void setExpiresIn(int expiresIn)
  {
    this.expiresIn = expiresIn;
  }
}