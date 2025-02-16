package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

/**
 * @author 47081
 * @version 1.0
 * @description 温度统计信息
 * @date 2021/2/22
 */
public class NET_TEMPERATURE_STATISTICS_INFO extends NetSDKLib.SdkStructure {
  /** 总人数 */
  public int nTotalCount;
  /** 超的温度次数 */
  public int nHighTempCount;
  /** 低的温度次数 */
  public int nLowTempCount;
  /** 体温正常次数 */
  public int nNormalTempCount;
  /** 未带口罩总人数 */
  public int nNoMaskCount;
  /** 记录编号 */
  public int nTimeKey;
  /** 预留字段 */
  public byte[] byReserved = new byte[1024];
}
